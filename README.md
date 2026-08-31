# integration-test

This branch combines the core play engine with the bidding system, to prove that bidding and play work correctly together as one coherent game — not just as isolated, independently-tested pieces.

## Classes

### Play engine (`logic` package)
| Class | Purpose |
|---|---|
| `Suit` | The four card suits. |
| `Rank` | Card ranks, Two through Ace. |
| `Strain` | What a bid can be made in: the 4 suits, plus No Trump. |
| `Card` | A single, immutable playing card. |
| `Deck` | A standard 52-card deck; supports shuffling. |
| `PlayerPosition` | The four seats at the table, in clockwise order. |
| `PlayerHand` | A player's current cards, kept sorted. |
| `PlayedCard` | Ties a played card to the player who played it. |
| `Trick` | An active trick in progress: cards played, suit led, leader. |
| `PlayValidation` | Follow-suit legality checks and trick-winner/trump-override logic. |
| `GameState` | Ties hands, tricks, and turn order together; drives play via `playCard()`. |

### Bidding system (`BiddingSystem` package)
All of Konke's bidding classes: player representation, bid actions (contract bids and passes), bidding history, and the bidding manager that drives turn order and determines the contract, declarer, and trump suit/strain.

### Testing and integration
| Class | Purpose |
|---|---|
| `PlayValidationTest` | JUnit tests covering follow-suit legality and trick-winner logic, including trump override and No Trump games. |
| `GameStateTest` | JUnit tests covering turn enforcement, legal/illegal play handling, trick completion, and a full 13-trick hand played end to end. |
| `Driver` | A manual integration driver: runs a scripted bidding sequence through `BiddingManager`, then hands off the resulting declarer and trump suit to `GameState` to play a full hand — proving both systems connect correctly. |

## Status

Bidding and play are both implemented and tested. `PlayValidationTest` and `GameStateTest` pass in full (14/14). `Driver` confirms a complete bid-then-play cycle runs correctly end to end. GUI integration is still in progress on a separate track.
