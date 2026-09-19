# BridgeTut

A Bridge game with a learning/tutorial focus: a Java backend (bidding, card play, a tutorial engine, and a double-dummy solver) driving a Python/Tkinter GUI over Py4J.

This branch (`integratingBackendToGUI`) is the integration point for the whole system — it brings together bidding, live card play, the tutorial engine, and the optimal-trick-count solver behind one GUI, backed by a single Py4J connection.

## Components

| Area | Where | What it does |
|---|---|---|
| Core game model | `src/logic` | `GameState`, `PlayerHand`, `Trick`, `Card`, `PlayValidation` — the rules-enforcement layer every other component builds on. |
| Bidding | `src/BiddingSystem/BiddingLogic`, `src/BiddingSystem/BiddingData` | `BiddingManager` drives the auction (bids, doubles, redoubles, pass-outs) and determines the contract and declarer. |
| Live play | `src/BiddingSystem/PlayingGateway.java` | Wraps a `GameState` for the play phase once bidding ends; exposes card play, trick tracking, and the live optimal-trick-count solve to Python. |
| Py4J entry point | `src/BiddingSystem/BiddingGateway.java` | The single object the GUI connects to. Wires bidding → play phase → tutorial gateway, translating everything to primitives (int/String/boolean/List) since Py4J can't pass Java objects directly to Python. |
| Tutorial engine | `src/LessonTutorial`, `src/BiddingSystem/Tutorial` | Parses `.txt` lesson files (bid-and-play and play-only formats) and replays them interactively: the computer plays West/North/East, South's bids and cards are validated against the lesson, mistakes are tracked. |
| Double-dummy solver | `src/DoubleDummySolverSystem` | `DoubleDummySolver.solve()` — minimax + alpha-beta pruning + a transposition table, answering "how many of the remaining tricks can the declaring side guarantee with perfect play." Surfaced live in the GUI from trick 2 onward as "Declarer's optimal tricks." |
| GUI | `src/GUI` | `GUI.py` (main game + login/signup), `Tutorial.py` (tutorial mode), `ResultPage.py` (game history), `db.py` (MySQL persistence). |

## Architecture

```
┌─────────────────┐         Py4J (socket, localhost)         ┌──────────────────────┐
│   GUI.py         │ ───────────────────────────────────────▶ │  BiddingGateway.java  │
│  (Tkinter)        │ ◀─────────────────────────────────────── │  (entry point)         │
└─────────────────┘                                            └──────────┬───────────┘
                                                                            │
                                                                 wraps      ▼
                                                                 BiddingManager → PlayingGateway → TutorialGateway
                                                                 (auction, then live play, then lesson replay)
```

`BiddingGateway` is a thin wrapper — it does not duplicate any bidding/play logic. It translates between primitives (int/String/boolean/List, which Py4J can pass across languages) and the existing Java classes. All actual rule enforcement lives in `BiddingManager`/`BiddingValidator`/`BiddingHistory`/`GameState`/`PlayValidation`.

## Running it

The fastest way: **`./run.command`** from the repo root (or double-click it in Finder). It compiles the Java sources, starts the `BiddingGateway` Py4J server, waits until it's actually reachable, checks the MySQL connection, then launches the GUI — and shuts the backend down automatically when the GUI closes.

To run the pieces manually instead:
1. **Start the Java gateway first.** Run `BiddingGateway.main()`. Confirm the console prints `"BiddingGateway started, listening for Python connections..."` before starting Python — if Python connects before this is up, it will fail to connect.
2. **Start the GUI.** Run `GUI.py` from inside `src/GUI` (it does sibling imports like `from Tutorial import ...`, so it must run from that directory). It connects to the gateway on startup via `JavaGateway()` (default port 25333).

Restart **both** after any Java-side change — Py4J does not hot-reload.

## Setup Requirements

### Java side
- Py4J's Java jar must be on the classpath (separate from the Python `pip install py4j` — they're two different packages of the same library). Find it via:
  ```
  python3 -c "import sys, os, glob; print(glob.glob(os.path.join(sys.prefix, 'share', 'py4j', '*.jar'))[0])"
  ```
  (`run.command` does this automatically.)

### Python side
- `pip install py4j` (or `python3 -m pip install py4j` to guarantee it targets the active interpreter — matters if multiple Python installs exist on the machine).
- Your IDE's Python interpreter must point at the **same interpreter** used in Terminal, or packages installed via Terminal won't resolve in the IDE (`sys.executable` from both should match exactly).
- `tkinter` is bundled with Python itself, not pip-installed — if unresolved, verify with `python3 -c "import tkinter"` from Terminal first before assuming an IDE issue.

### Database (local MySQL, for login/signup/game history)
`db.py` connects to a local MySQL server (`127.0.0.1:3306`, user `root`, password `bridge`, database `Bridge`).

1. Install MySQL if not already present: `brew install mysql`, then `brew services start mysql`.
2. Fresh installs typically have no root password set. Connect with `mysql -u root`, then run:
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'bridge';
   FLUSH PRIVILEGES;
   ```
3. Create the database if not already present:
   ```sql
   CREATE DATABASE Bridge;
   ```
   The full table schema (users, games, tricks, bids) lives with whoever owns the database work — confirm against the authoritative schema rather than reverse-engineering it from `db.py`'s queries.

## Tests

JUnit 5 tests live in both `test/` (the current convention) and a few remaining `src/**/tests` locations for classes without a `test/` equivalent yet (`BiddingManagerTest`, `BiddingGatewayTest`, `CopyTest`). No build tool is configured in this repo — compile and run them with `javac`/`java` against the JUnit Platform jars, or via your IDE's test runner.

## Known Issues / Not Yet Implemented

- **Claim/Concede can be blocked from ever succeeding in the tutorial engine.** `TutorialEngine.claim()`/`concede()` both require `allTricksPlayed`, but `isTutorialComplete()` already returns `true` under that same condition and is checked first — so the guard is unreachable once a lesson's listed tricks are fully played. Needs a design decision on what "tutorial complete" should mean relative to claim/concede.
- **No persistence of passed-out hands.** `GameReset.resetGame()` discards the auction history on redeal (`"maybe should post the game history to the database beforehand"` — not implemented).
- **Tutorial lesson file paths are placeholders.** `Tutorial.py`'s `load_tutorial()` points at `BiddingAndPlayTutorial.txt` / `PlayTutorial.txt`, which don't exist in the repo yet — needs real lesson files at those paths (or the paths updated to match wherever they end up).
- **Database schema is unconfirmed** — needs confirming against the authoritative schema rather than what's been reverse-engineered from `db.py`'s queries to unblock local testing.
- **Multiple Python interpreters on a dev machine can cause "unresolved import" errors in an IDE** even when packages are correctly `pip install`ed — verify the IDE's Python SDK path matches `sys.executable` from Terminal if this recurs.

Double/Redouble bidding and the opening lead after bidding ends are both fully implemented (contrary to earlier notes in this doc's history) — `BiddingGateway.submitDouble()`/`submitRedouble()` are wired to the GUI's Dbl/Redbl buttons, and `Trick.startTrick()` correctly seats the opening leader to the declarer's left.

## `BiddingGateway` API Reference

| Method | Returns | Notes |
|---|---|---|
| `submitBid(int level, String strainName)` | `boolean` | `strainName` matches `Strain` enum names exactly: `CLUBS`, `DIAMONDS`, `HEARTS`, `SPADES`, `NO_TRUMP` |
| `submitPass()` / `submitDouble()` / `submitRedouble()` | `boolean` | |
| `getCurrentSeatIndex()` | `int` | `PlayerPosition.ordinal()`: SOUTH=0, WEST=1, NORTH=2, EAST=3 |
| `getCurrentPlayerName()` | `String` | |
| `checkBiddingOver()` | `boolean` | Call after every accepted action |
| `isPassedOut()` | `boolean` | Only meaningful when `checkBiddingOver()` is `true` |
| `getDeclarerName()` / `getDeclarerSeatIndex()` | `String` / `int` | Only meaningful when bidding ended with a contract (not passed out) |
| `getWinningContractString()` | `String` | e.g. `"2 HEARTS"` |
| `resetAfterPassedOut()` | `int` | Re-deals, replaces internal manager, returns new starting seat index. Also used to start a fresh game from the menu, not just after a pass-out. |
| `getHandForSeat(int seatIndex)` | `List<String>` | Card codes matching the GUI's asset naming: `<suit letter><rank>`, e.g. `"D6"`, `"CK"`, `"C10"` |
| `startPlayPhase()` | `PlayingGateway` | Call once bidding ends with a real contract; hands off to live card play. |
| `getTutorialGateway()` | `TutorialGateway` | Entry point for the tutorial system. |

`PlayingGateway.getOptimalTricksForDeclaringSide()` is a blocking call — can take from milliseconds up to a minute or more depending on how many cards remain — so it must be called off the GUI's main thread (see `GUI.py`'s `update_optimal_tricks_display`, which runs it in a background `threading.Thread`).
