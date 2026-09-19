package logic;

/**
 * Enumerates the thirteen standard playing card ranks in ascending value order (TWO through ACE).
 */
public enum Rank {
    TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;

    /**
     * Gets the standard 1-character letter/number symbol representing this rank.
     *
     * @return String representation (e.g., "2"-"10", "J", "Q", "K", "A").
     */
    public String getRankLetter() {
        switch (this) {
            case JACK: return "J";
            case QUEEN: return "Q";
            case KING: return "K";
            case ACE: return "A";
            default: return String.valueOf(ordinal() + 2); 
        }
    }
}