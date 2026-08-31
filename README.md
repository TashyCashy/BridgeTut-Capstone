# design-and-implement-data-structures-for-trick-playedcard-and-turn-sequence

This branch covers the original foundational data structures for the play phase: representing an active trick, cards played, and player turn order — built before rule-enforcement logic (legality checks, trick-winner determination) existed.

## Classes

| Class | Purpose |
|---|---|
| `Suit` | The four card suits (Clubs, Diamonds, Hearts, Spades). |
| `Rank` | Card ranks, Two through Ace, in ascending order. |
| `Card` | A single, immutable playing card (suit + rank). |
| `Deck` | A standard 52-card deck. |
| `PlayerPosition` | The four seats at the table, declared in clockwise play order. |
| `PlayerHand` | A player's current cards, kept sorted by suit then rank. |
| `Trick` | Represents an active trick: cards played so far, the suit led, and the leader. Includes logic to determine the opening lead from the Declarer's position. Deliberately holds state only — no rule enforcement yet. |

## Status

Data structures are implemented and documented, ready for trick-execution logic to be built on top of them. This branch corresponds to the original GitLab issue defining these structures; later work (legality checks, trick-winner logic, full game state) was built on `corePlayEngine`.
