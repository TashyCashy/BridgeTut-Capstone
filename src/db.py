import mysql.connector
from mysql.connector import Error

def get_connection():
    """Setting up connection to database"""
    try:
        return mysql.connector.connect(
            host="localhost",
            port=3306,
            user="root",
            password="bridge",
            database="Bridge"
        )
    except Error as e:
        print(f"Database connection failed: {e}")
        return None