# Integration Branch — GUI ↔ Java Backend

This branch connects the Python Tkinter GUI to the Java bidding backend via **Py4J**, and documents the local MySQL setup required to run login/signup.

## Architecture

```
┌─────────────────┐         Py4J (socket, localhost)         ┌──────────────────────┐
│   GUI.py         │ ───────────────────────────────────────▶ │  BiddingGateway.java  │
│  (Tkinter)        │ ◀─────────────────────────────────────── │  (entry point)         │
└─────────────────┘                                            └──────────┬───────────┘
                                                                            │
                                                                 wraps      ▼
                                                                 BiddingManager
                                                                 (existing, tested
                                                                  bidding backend)
```

`BiddingGateway` is a thin wrapper — it does not duplicate any bidding logic. It translates between primitives (int/String/boolean, which Py4J can pass across languages) and the existing Java bidding classes. All actual rule enforcement still lives in `BiddingManager`/`BiddingValidator`/`BiddingHistory`, unchanged from the bidding-only branch.

## Running It

Two processes must be running simultaneously:

1. **Start the Java gateway first.** Run `BiddingGateway.main()`. Confirm the console prints `"BiddingGateway started, listening for Python connections..."` before proceeding — if Python connects before this is up, it will fail to connect.
2. **Start the GUI.** Run `GUI.py`. It connects to the gateway on startup via `JavaGateway()` (default port 25333).

Restart **both** processes after any Java-side change — Py4J does not hot-reload.

## Setup Requirements

### Java side
- Py4J's Java jar must be on the module's classpath (separate from the Python `pip install py4j` — they are two different packages of the same library). Find the jar via:
  ```
  python3 -c "import py4j, os; print(os.path.dirname(py4j.__file__))"
  ```
  then look for `share/py4j/py4j*.jar` under that path, or add via Maven if the project uses it.

### Python side
- `pip install py4j` (or `python3 -m pip install py4j` to guarantee it targets the active interpreter — matters if multiple Python installs exist on the machine).
- IntelliJ's Python module SDK must point at the **same interpreter** used in Terminal, or packages installed via Terminal won't resolve in the IDE (`sys.executable` from both should match exactly).
- `tkinter` is bundled with Python itself, not pip-installed — if unresolved, verify with `python3 -c "import tkinter"` from Terminal first before assuming an IDE issue.

### Database (local MySQL, for login/signup)
`db.py` connects to a local MySQL server (`127.0.0.1:3306`, user `root`, password `bridge`, database `Bridge`) for the login/signup screens.

1. Install MySQL if not already present: `brew install mysql`, then `brew services start mysql`.
2. Fresh installs typically have no root password set. Connect with `mysql -u root`, then run:
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'bridge';
   FLUSH PRIVILEGES;
   ```
3. Create the database/table if not already present (schema below is inferred from `db.py`'s queries — **confirm the authoritative schema with whoever owns the database work** before treating this as final):
   ```sql
   CREATE DATABASE Bridge;
   USE Bridge;
   CREATE TABLE users (
       user_id INT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(255) UNIQUE NOT NULL,
       password_hash VARCHAR(255) NOT NULL
   );
   ```

## What Changed in `GUI.py`

Kept as close to the original as possible — most of the file (frame layout, image resizing, button placement, page navigation) is untouched. The changes made were:

1. **Added, at the top of the file** — the Py4J connection and a suit-symbol translation table:
   ```python
   gateway = JavaGateway()
   entry_point = gateway.entry_point
   SUIT_SYMBOL_TO_STRAIN = {"♣": "CLUBS", "♦": "DIAMONDS", "♥": "HEARTS", "♠": "SPADES", "NT": "NO_TRUMP"}
   ```

2. **`self.players` reordered** from `["West","North","East","South"]` to `["South","West","North","East"]`. This must match `logic.PlayerPosition`'s declared enum order exactly, since the backend indexes seats by `PlayerPosition.ordinal()`. Getting this order wrong does not throw an error — it silently displays the wrong player's name for a given seat.

3. **`make_bid` rewritten** to call the Java backend rather than tracking turn order, pass counts, and auction-end conditions locally in Python. Local bookkeeping (bid history display, contract label) is preserved; only *decision-making* (is this legal, whose turn is it now, has the auction ended, who's the declarer) was moved to Java, since duplicating that logic in two languages risked the two versions silently disagreeing over time.

4. **`player_hands` rewritten** to fetch each seat's real dealt cards from `getHandForSeat(seatIndex)` instead of rendering 13 copies of a single hardcoded placeholder card per side. Button creation, image resizing, and layout (`pack` for North/South, `place` for East/West) are otherwise unchanged from the original.

5. **Fixed a pre-existing typo** in `play_card`: `self.player[...]` → `self.players[...]`. Unrelated to Py4J, but it would crash the first time a real card (post-hand-rendering) was clicked, so it was fixed while working in this area.

## `BiddingGateway` API Reference

| Method | Returns | Notes |
|---|---|---|
| `submitBid(int level, String strainName)` | `boolean` | `strainName` matches `Strain` enum names exactly: `CLUBS`, `DIAMONDS`, `HEARTS`, `SPADES`, `NO_TRUMP` |
| `submitPass()` | `boolean` | Always `true` under current rules |
| `getCurrentSeatIndex()` | `int` | `PlayerPosition.ordinal()`: SOUTH=0, WEST=1, NORTH=2, EAST=3 |
| `getCurrentPlayerName()` | `String` | |
| `checkBiddingOver()` | `boolean` | Call after every accepted action |
| `isPassedOut()` | `boolean` | Only meaningful when `checkBiddingOver()` is `true` |
| `getDeclarerName()` / `getDeclarerSeatIndex()` | `String` / `int` | Only meaningful when bidding ended with a contract (not passed out) |
| `getWinningContractString()` | `String` | e.g. `"2 HEARTS"` |
| `resetAfterPassedOut()` | `int` | Re-deals, replaces internal manager, returns new starting seat index |
| `getHandForSeat(int seatIndex)` | `List<String>` | Card codes matching the GUI's asset naming: `<suit letter><rank>`, e.g. `"D6"`, `"CK"`, `"C10"` |

## Known Issues / Not Yet Implemented

- **Double and Redouble** — not implemented on the Java side yet (planned as a stretch addition beyond the assignment's stated scope). The GUI's "Dbl" button currently triggers a "not supported yet" message rather than being wired to the backend.
- **Database schema is unconfirmed** — the `users` table above was reverse-engineered from `db.py`'s queries to unblock local testing. Needs confirming against the authoritative schema.
- **No persistence of passed-out hands** — `GameReset` discards auction history on redeal with a `TODO` marking where a database write should happen; not implemented.
- **Opening lead (who plays first after bidding ends) is not implemented** — this is a play-phase concern, separate from declarer determination, and not yet built.
- **Multiple Python interpreters on the dev machine can cause "unresolved import" issues in IntelliJ** even when packages are correctly `pip install`ed — always verify IntelliJ's Python SDK path matches `sys.executable` from Terminal if this recurs.
