import mysql.connector
from mysql.connector import Error
import bcrypt

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

    
