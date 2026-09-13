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

    cursor.execute("SELECT user_id FROM users WHERE username = %s", (username,))
    if cursor.fetchone():
        cursor.close()
        conn.close()
        return False

    hashed = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())

    try:
        cursor.execute("INSERT INTO users (username, password_hash) VALUES (%s, %s)", (username, hashed.decode("utf-8")))
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
    cursor.execute("SELECT password_hash FROM users WHERE username = %s", (username,))
    row = cursor.fetchone()
    cursor.close()
    conn.close()

    if row is None:
        return False

    stored_hash=row[0].encode("utf-8")
    if bcrypt.checkpw(password.encode("utf-8"), stored_hash):
        return True
    else:
        return False

def create_game(user_id, dealer):
    conn = get_connection()

    if conn is None:
        return None

    cursor= conn.cursor()

    try:
        date_played = date.today()
        cursor.execute("SELECT COALESCE(MAX(seq_num), 0) "
               "FROM games "
               "WHERE user_id = %s AND date_played = %s",
               (user_id, date_played))
        max_seq = cursor.fetchone()[0]
        seq_num = max_seq + 1
        attempt_num = 1

        NS_score=0
        EW_score=0
        
        cursor.execute("INSERT INTO games "
                       "(user_id, dealer, seq_num, attempt_num, vulnerability, NS_score, EW_score, date_played) "
                       "VALUES (%s, %s, %s,%s, %s,%s, %s, %s)", 
                       (user_id, dealer, seq_num, attempt_num, None, NS_score, EW_score, date_played))

        conn.commit()
        return cursor.lastrowid
    except Exception as e:
        conn.rollback()
        print(f"Error creating game : {e}")
        return None
    finally: 
        cursor.close()
        conn.close()

def get_user_id(username):
    conn = get_connection()
    if conn is None:
        return False
    
    cursor = conn.cursor()
    
    try:
        cursor.execute("SELECT user_id FROM users WHERE username = %s", (username,))
        row = cursor.fetchone()
        
        if row:
            return row[0]
        return None
    except Exception as e:
        conn.rollback()
        print(f"Error getting user ID: {e}")
        return None
    finally:
        cursor.close()
        conn.close()

def save_bid(game_id, position, bid_value):
    conn = get_connection()

    if conn is None:
        return None
    
    cursor= conn.cursor()

    try:
        cursor.execute("INSERT INTO bids (game_id, bid_value, position) VALUES (%s, %s, %s)",( game_id, bid_value, position))
        conn.commit()
        return True
    except Exception as e:
        conn.rollback()
        print(f"Error saving bid : {e}")
        return None
    finally: 
        cursor.close()
        conn.close()

def get_bidding_hist(game_id):
    conn = get_connection()

    if conn is None:
        return []
    
    cursor= conn.cursor()

    try:
        cursor.execute("SELECT bid_value"
        "FROM bids"
         "WHERE game_id = (%s)"
          "ORDER by bid_id", (game_id,))
        conn.commit()
        return cursor.fetchall()
    except Exception as e:
        conn.rollback()
        print(f"Error getting bidding history : {e}")
        return None
    finally: 
        cursor.close()
        conn.close()

def save_trick(game_id, trick_winner, winner):
    conn = get_connection()
    
    if conn is None:
        return None
    
    cursor= conn.cursor()
    
    try:
        cursor.execute("INSERT INTO bids (game_id, trick_winner, winner)"
                       "VALUES (%s, %s, %s)",
                       ( game_id, trick_winner, winner))
        conn.commit()
        return cursor.lastrowid
    except Exception as e:
        conn.rollback()
        print(f"Error saving trick : {e}")
        return None
    finally: 
        cursor.close()
        conn.close()

def save_card_played(suit, card_rank, trick_id):
    conn = get_connection()
    
    if conn is None:
        return None
    
    cursor= conn.cursor()
    
    try:
        cursor.execute("INSERT INTO bids (suit, card_rank, trick_id)"
                       "VALUES (%s, %s, %s)",
                       ( suit, card_rank, trick_id))
        conn.commit()
        return True
    except Exception as e:
        conn.rollback()
        print(f"Error saving trick : {e}")
        return None
    finally: 
        cursor.close()
        conn.close()

def update_results(game_id, declarer, ns_score, ew_score):
    conn = get_connection()
    
    if conn is None:
        return None
    
    cursor= conn.cursor()
    
    try:
        cursor.execute("UPDATE games " 
                       "SET declarer = %s, NS_score = %s, EW_score = %s"
                       "WHERE game_id=%s",
                       ( declarer, ns_score, ew_score, game_id))
        conn.commit()
        return True
    except Exception as e:
        conn.rollback()
        print(f"Error updating game result : {e}")
        return None
    finally: 
        cursor.close()
        conn.close()




    
