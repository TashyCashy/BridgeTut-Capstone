package logic;

/**
 * Enumerates the four standard card suits in Bridge (Clubs, Diamonds, Hearts, Spades).
 */
public enum Suit {
    CLUBS, DIAMONDS, HEARTS, SPADES;

    /**
     * Returns the 1-character letter symbol representing this suit.
     * Used for card code generation and GUI image file mapping (e.g., "C", "D", "H", "S").
     *
     * @return Single-character string code of the suit.
     */
    public String getSuitLetter() {
        switch (this) {
            case CLUBS: return "C";
            case DIAMONDS: return "D";
            case HEARTS: return "H";
            case SPADES: return "S";
            default: throw new IllegalStateException("Unknown suit value: " + this);
        }
    }

    /**
     * Converts this card suit into its corresponding bidding {@link Strain}.
     *
     * @return The matching bidding {@link Strain}.
     */
    public Strain toStrain() {
        switch (this) {
            case CLUBS:
                return Strain.CLUBS;
            case DIAMONDS:
                return Strain.DIAMONDS;
            case HEARTS:
                return Strain.HEARTS;
            case SPADES:
                return Strain.SPADES;
            default:
                throw new IllegalStateException("Unknown suit value: " + this);
        }
    }
}