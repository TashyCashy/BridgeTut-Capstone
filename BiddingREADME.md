# Bidding System

This module implements the auction (bidding) phase of the bridge game backend. It tracks turn order across the four seats, validates proposed bids against the rules of contract bridge, detects when the auction ends (either with a final contract or a passed-out hand), and determines the declarer once bidding concludes.

## Package Structure

```
BiddingSystem/
├── DealCards.java                   # Deals cards to all players from a shuffled deck
├── Player.java                      # A seated player: name, hand, position
├── BiddingLogic/
│   ├── BiddingManager.java          # Orchestrates turns, logs actions, detects auction end
│   ├── BiddingValidator.java        # Stateless legality checks for a proposed action
│   └── GameReset.java               # Re-deals and constructs a fresh BiddingManager after a passed-out hand
└── BiddingData/
    ├── BiddingHistory.java          # Ordered log of every action taken during the auction
    ├── BidEntry.java                # One (action, player) pair in the history
    └── Actions/
        ├── PlayerAction.java        # Abstract parent — just declares getDisplayString()
        ├── PassAction.java          # A pass; always legal
        └── ContractBid.java         # A bid: level (1–7) + strain (suit or No Trump)
```

Shared, phase-agnostic classes this module depends on (owned outside `BiddingSystem`, in `logic`):
- `PlayerPosition` — enum `SOUTH, WEST, NORTH, EAST`; provides `next()` (seat to the left) and `partner()` (seat across the table). This is the single source of truth for "which seat" — used by both the bidding phase and, later, the play phase.
- `Strain` — enum `CLUBS, DIAMONDS, HEARTS, SPADES, NO_TRUMP`; represents what a *contract* is played in. Provides `toSuit()`, which returns the matching `Suit` for a suit contract or `null` for No Trump.
- `Suit` — enum `CLUBS, DIAMONDS, HEARTS, SPADES`; represents the suit of a physical playing *card*. Deliberately kept separate from `Strain` so that "No Trump" can never be mistakenly assigned to a card — an illegal state that's made structurally impossible by having two distinct types.
- `Deck`, `PlayerHand`, `Card`, `Rank` — dealing and hand representation.
- `DealCards` — deals a shuffled `Deck` out to a `Player[]` array, 13 cards each, round-robin.

## How the Auction Works

### Turn order

`BiddingManager` holds `currentSeat: PlayerPosition`. Each successful action calls `advanceTurn()`, which delegates to `PlayerPosition.next()` — always the seat to the current player's left. `getCurrentPlayer()` looks up the player at that seat via `players[currentSeat.ordinal()]`.

**Important constraint:** the `Player[]` array passed into `BiddingManager`'s constructor *must* be built in `PlayerPosition.values()` order — `SOUTH, WEST, NORTH, EAST` — not the more intuitive North-first order. Building the array in the wrong order will not throw an error; it will silently produce incorrect turn order, since array indexing relies entirely on `PlayerPosition.ordinal()` matching array position.

### Playing an action

`BiddingManager.ActionPlayed(PlayerAction p)`:
1. Calls `BiddingValidator.validateBid(p, biddingHistory)` — **before** any state changes.
2. If legal: logs a new `BidEntry` (pairing the action with `getCurrentPlayer()`), advances the turn, returns `true`.
3. If illegal: does nothing further and returns `false`.

This ordering guarantees an illegal action never costs a player their turn and never pollutes the history.

### Legality rules (`BiddingValidator`)

`BiddingValidator` is stateless — it takes the current `BiddingHistory` as a parameter rather than owning one itself, so there is exactly one source of truth for auction state (the one `BiddingManager` holds).

- **Pass** is always legal.
- **Contract bid**: legal only if `checkNoContractBidMade()` is true (no bid exists yet — anything is legal), *or* the proposed bid `isBigger()` than the current highest bid on record.

### Bid comparison (`ContractBid.isBigger`)

Given `this` bid and `otherBid`:
1. If `otherBid`'s level is higher → `otherBid` wins.
2. If `this`'s level is higher → `this` wins.
3. If levels are equal → compare strain by `ordinal()`. Because `Strain`'s declaration order is `CLUBS < DIAMONDS < HEARTS < SPADES < NO_TRUMP`, this ordinal comparison already matches real bridge ranking (No Trump outranks every suit) with no special-casing required.
4. An exactly equal bid (same level, same strain) is correctly treated as *not* bigger — you cannot re-bid the exact same contract.

**This ordinal-based comparison is a load-bearing implicit dependency on `Strain`'s declaration order.** If that enum is ever reordered for an unrelated reason, bid comparisons will silently break with no compiler error. Do not reorder `Strain`'s constants.

### Finding the current highest bid (`BiddingHistory.getLargestBid`)

Scans the full history, skipping any entry that isn't a `ContractBid` (i.e., skipping passes), and returns the highest `ContractBid` seen. Returns `null` if no contract bid has been made yet — callers must handle this case explicitly (both `BiddingValidator` and `BiddingManager` do).

### Auction-end detection (`BiddingManager.checkBiddingOver`)

Called after every successful `ActionPlayed`. Two independent, mutually exclusive terminal conditions:

| Condition | Meaning | Result |
|---|---|---|
| A contract bid exists **and** 3 consecutive passes follow it | Nobody topped the highest bid | Auction ends with a final contract |
| No contract bid exists at all **and** 4 consecutive passes have occurred | Nobody bid anything | Hand is **passed out** |

Both branches return `true` from `checkBiddingOver()`. The caller must check `isPassedOut()` afterward to distinguish which terminal condition occurred, since a plain boolean can't carry that distinction on its own.

On a contract-final ending, three pieces of state get captured:
- `winningContract` — the full `ContractBid` (level + strain) that won.
- `declarer` — see below.

### Declarer determination (`BiddingHistory.determineDeclarer`)

**This is not simply "whoever made the highest/final bid."** Bridge's actual rule: within the winning partnership, the declarer is whichever partner bid the winning **strain** *first*, even if their partner later re-bid that same strain at a higher level.

`determineDeclarer(Strain winningStrain, Player winningBidder)` walks the bidding history **forward** from the start (not backward, unlike the pass-counting logic) and returns the player at the first `ContractBid` entry where:
- the strain matches `winningStrain`, **and**
- the player is either `winningBidder` or `winningBidder.getPosition().partner()`.

Example: North opens 1♥, South later raises to 2♥, and 2♥ becomes the final contract. The declarer is **North** (bid Hearts first between the partnership), not South, even though South made the actual winning bid.

### Passed-out hands (`GameReset`)

If `checkBiddingOver()` returns `true` with `isPassedOut() == true`, the current `BiddingManager` instance is discarded. The caller invokes:

```java
manager = GameReset.resetGame(manager);
```

`GameReset.resetGame`:
1. Clears every player's hand.
2. Builds and shuffles a fresh `Deck`.
3. Re-deals via `DealCards.dealHands`.
4. Constructs and returns a **brand-new** `BiddingManager`, with the new dealer set to `manager.getStartingPosition().next()` — the player to the left of whoever dealt the passed-out hand, per standard bridge convention.

A fresh `BiddingManager` is constructed (rather than resetting the old one's fields in place) so that every piece of auction state — history, current seat, validator, winner/declarer fields — starts unambiguously clean, with no risk of a stray leftover field from the discarded auction.

**Known gap:** persisting the passed-out hand's history to the database before it's discarded is not yet implemented (marked with a `TODO` in `GameReset`). This is intentionally deferred to whoever owns the database work.

## What's Implemented

- Bid legality (level and strain comparison, including No Trump)
- Pass handling
- Turn order via `PlayerPosition`
- Illegal-action rejection with no state corruption
- 3-consecutive-pass auction-end detection with correct winning contract capture
- 4-consecutive-pass passed-out detection
- Correct declarer determination (first-partner-to-bid-the-strain rule, not just highest bidder)
- Passed-out re-deal via `GameReset`
- Full JUnit test coverage of the above (see `BiddingManagerTest`)

## Known Gaps / Deliberately Deferred

- **Double and Redouble** — not implemented. Some bridge variants omit these, and they were deliberately left out of this prototype to prioritize the core bid/pass/auction-end/declarer logic under a tight deadline.
- **Database logging of passed-out hands** — flagged with a `TODO` in `GameReset`, deferred to the teammate owning database work.
- **Opening lead / play-phase handoff** — determining who leads the first trick (the player to declarer's left) is a play-phase concern, not implemented in this module. Do not confuse this with declarer determination — they are different rules.
- **`Player`/`PlayerHand` position field reconciliation** — `Player` now stores a `PlayerPosition` (migrated from an earlier, incompatible raw-`int` seat convention that did not match `PlayerPosition`'s ordinal ordering). Confirm with the teammate owning `PlayerHand` that no lingering dual-representation issue remains.

## Testing

`BiddingManagerTest` (JUnit 5) covers, per the module's acceptance criteria:
- Legal bids at every level (1–7) and every strain, including No Trump
- Pass legality in all history states
- Rejection of lower-level, same-level-lower-strain, and exact-repeat bids
- Turn order advancing correctly, and *not* advancing on a rejected action
- Both auction-end conditions (3-pass contract-final, 4-pass passed-out)
- Declarer determination for the case where the naive "highest bidder" answer and the correct bridge-rule answer diverge (partner bids the winning strain first, then re-bids it higher)
- A seeded randomized ("fuzz") sequence of legal-only actions, confirming no exceptions are thrown across an extended auction

Run via your IDE's JUnit runner, or `mvn test` / `gradle test` depending on your build setup.
