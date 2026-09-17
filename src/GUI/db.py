import mysql.connector
from mysql.connector import Error
import bcrypt
from datetime import date


def get_connection():
    """Setting up connection to database"""
    try:
        return mysql.connector.connect(
            host="127.0.0.1",
            port=3306,
            user="root",
            password="bridge",
            database="Bridge"
        )
    except Error as e:
        print(f"Database connection failed: {e}")
        return None


def create_user(username, password):
    """Adding new user to the database and creating hashed password"""
    conn = get_connection()

    if conn is None:
        return False

    cursor = conn.cursor()

    cursor.execute(
        "SELECT user_id FROM users WHERE username = %s",
        (username,)
    )

    if cursor.fetchone():
        cursor.close()
        conn.close()
        return False

    hashed = bcrypt.hashpw(
        password.encode("utf-8"),
        bcrypt.gensalt()
    )

    try:
        cursor.execute(
            "INSERT INTO users (username, password_hash) "
            "VALUES (%s, %s)",
            (username, hashed.decode("utf-8"))
        )

        conn.commit()
        return True

    except Exception as e:
        conn.rollback()
        print(f"Error creating account: {e}")
        return False

    finally:
        cursor.close()
        conn.close()


def verify_user(username, password):
    """Moving through database to ensure correct username and password entered."""
    conn = get_connection()

    if conn is None:
        return False

    cursor = conn.cursor()

    cursor.execute(
        "SELECT password_hash FROM users WHERE username = %s",
        (username,)
    )

    row = cursor.fetchone()

    cursor.close()
    conn.close()

    if row is None:
        return False

    stored_hash = row[0].encode("utf-8")

    if bcrypt.checkpw(
        password.encode("utf-8"),
        stored_hash
    ):
        return True
    else:
        return False


def get_user_id(username):
    conn = get_connection()

    if conn is None:
        return None

    cursor = conn.cursor()

    try:
        cursor.execute(
            "SELECT user_id FROM users WHERE username = %s",
            (username,)
        )

        row = cursor.fetchone()

        if row:
            return row[0]

        return None

    except Exception as e:
        print(f"Error getting user ID: {e}")
        return None

    finally:
        cursor.close()
        conn.close()


def create_game(user_id, dealer):
    conn = get_connection()

    if conn is None:
        return None

    cursor = conn.cursor()

    try:
        date_played = date.today()

        cursor.execute(
            "SELECT COALESCE(MAX(seq_num), 0) "
            "FROM games "
            "WHERE user_id = %s AND date_played = %s",
            (user_id, date_played)
        )

        max_seq = cursor.fetchone()[0]
        seq_num = max_seq + 1
        attempt_num = 1

        cursor.execute(
            "INSERT INTO games "
            "(user_id, dealer, seq_num, attempt_num, date_played) "
            "VALUES (%s, %s, %s, %s, %s)",
            (
                user_id,
                dealer,
                seq_num,
                attempt_num,
                date_played
            )
        )

        conn.commit()
        return cursor.lastrowid

    except Exception as e:
        conn.rollback()
        print(f"Error creating game: {e}")
        return None

    finally:
        cursor.close()
        conn.close()


def save_bid(game_id, position, bid_value):
    conn = get_connection()

    if conn is None:
        return None

    cursor = conn.cursor()

    try:
        cursor.execute(
            "INSERT INTO bids (game_id, bid_value, position) "
            "VALUES (%s, %s, %s)",
            (game_id, bid_value, position)
        )

        conn.commit()
        return True

    except Exception as e:
        conn.rollback()
        print(f"Error saving bid: {e}")
        return None

    finally:
        cursor.close()
        conn.close()


def get_bidding_hist(game_id):
    conn = get_connection()

    if conn is None:
        return []

    cursor = conn.cursor()

    try:
        cursor.execute(
            "SELECT bid_value, position "
            "FROM bids "
            "WHERE game_id = %s "
            "ORDER BY bid_id",
            (game_id,)
        )

        return cursor.fetchall()

    except Exception as e:
        print(f"Error getting bidding history: {e}")
        return []

    finally:
        cursor.close()
        conn.close()


def save_trick(game_id, trick_number, winner):
    conn = get_connection()

    if conn is None:
        return None

    cursor = conn.cursor()

    try:
        cursor.execute(
            "INSERT INTO tricks (game_id, trick_number, winner) "
            "VALUES (%s, %s, %s)",
            (game_id, trick_number, winner)
        )

        conn.commit()
        return cursor.lastrowid

    except Exception as e:
        conn.rollback()
        print(f"Error saving trick: {e}")
        return None

    finally:
        cursor.close()
        conn.close()


def save_card_played(suit, card_rank, trick_id, play_order):
    conn = get_connection()

    if conn is None:
        return None

    cursor = conn.cursor()

    try:
        cursor.execute(
            "INSERT INTO cards_played "
            "(suit, card_rank, trick_id, play_order) "
            "VALUES (%s, %s, %s, %s)",
            (suit, card_rank, trick_id, play_order)
        )

        conn.commit()
        return True

    except Exception as e:
        conn.rollback()
        print(f"Error saving card: {e}")
        return None

    finally:
        cursor.close()
        conn.close()


def update_results(game_id, declarer):
    conn = get_connection()

    if conn is None:
        return None

    cursor = conn.cursor()

    try:
        cursor.execute(
            "UPDATE games "
            "SET declarer = %s "
            "WHERE game_id = %s",
            (declarer, game_id)
        )

        conn.commit()
        return True

    except Exception as e:
        conn.rollback()
        print(f"Error updating game result: {e}")
        return None

    finally:
        cursor.close()
        conn.close()


def get_game_dates(user_id):
    conn = get_connection()

    if conn is None:
        return []

    cursor = conn.cursor()

    try:
        cursor.execute(
            "SELECT DISTINCT date_played "
            "FROM games "
            "WHERE user_id = %s "
            "ORDER BY date_played DESC",
            (user_id,)
        )

        return [row[0] for row in cursor.fetchall()]

    except Exception as e:
        print(f"Error getting game dates: {e}")
        return []

    finally:
        cursor.close()
        conn.close()


def get_games_by_date(user_id, selected_date):
    conn = get_connection()

    if conn is None:
        return []

    cursor = conn.cursor()

    try:
        cursor.execute(
            "SELECT game_id, dealer, declarer, seq_num, attempt_num "
            "FROM games "
            "WHERE user_id = %s AND date_played = %s "
            "ORDER BY seq_num",
            (user_id, selected_date)
        )

        return cursor.fetchall()

    except Exception as e:
        print(f"Error getting games: {e}")
        return []

    finally:
        cursor.close()
        conn.close()


def get_game_tricks(game_id):
    conn = get_connection()

    if conn is None:
        return []

    cursor = conn.cursor()

    try:
        cursor.execute(
            "SELECT trick_id, trick_number, winner "
            "FROM tricks "
            "WHERE game_id = %s "
            "ORDER BY trick_number",
            (game_id,)
        )

        return cursor.fetchall()

    except Exception as e:
        print(f"Error getting tricks: {e}")
        return []

    finally:
        cursor.close()
        conn.close()


def get_game_cards(game_id):
    conn = get_connection()

    if conn is None:
        return []

    cursor = conn.cursor()

    try:
        cursor.execute(
            "SELECT cp.cards_id, cp.suit, cp.card_rank, "
            "cp.trick_id, cp.play_order, t.winner "
            "FROM cards_played cp "
            "JOIN tricks t ON cp.trick_id = t.trick_id "
            "WHERE t.game_id = %s "
            "ORDER BY cp.trick_id, cp.play_order",
            (game_id,)
        )

        return cursor.fetchall()

    except Exception as e:
        print(f"Error getting cards: {e}")
        return []

    finally:
        cursor.close()
        conn.close()