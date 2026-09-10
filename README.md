# corePlayEngine

This branch contains the core game-logic engine for the play phase of Bridge — everything needed to represent a deal, enforce legal card play, and determine trick winners, independent of the bidding system.

## Classes

| Class | Purpose |
|---|---|
| `Suit` | The four card suits (Clubs, Diamonds, Hearts, Spades). Deliberately excludes No Trump, since `Deck` relies on `Suit` to generate exactly 52 real cards. |
| `Rank` | Card ranks, Two through Ace, declared in ascending order so rank comparisons work via `compareTo()`. |
| `Strain` | What a bid can be made in: the 4 suits, plus No Trump. Kept separate from `Suit` so bidding can represent NT contracts without corrupting card generation. |
| `Card` | A single, immutable playing card (suit + rank). |
| `Deck` | A standard 52-card deck; supports shuffling. |
| `PlayerPosition` | The four seats at the table (South, West, North, East), declared in clockwise play order so `next()` gives the correct following player. |
| `PlayerHand` | A player's current cards, kept sorted by suit then rank. |
| `PlayedCard` | Ties a single played card to the player who played it — used for building trick/game history. |
| `Trick` | Represents an active trick in progress: cards played so far, the suit led, and who's leading. Deliberately holds state only — it does not enforce rules. |
| `PlayValidation` | Rule enforcement: checks whether a card play is legal (follow-suit rule) and determines the winner of a completed trick (including trump-suit override and No Trump handling). |
| `GameState` | Ties everything together: holds all 4 hands, the trick in progress, completed tricks, and drives play turn by turn via `playCard()`. |

## Status

Core play logic is implemented and functioning. This branch does not include the bidding system or any GUI integration — see `integration-test` for the combined, end-to-end version.
