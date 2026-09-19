package logic;

/**
 * Enumerates the five contract suit strain options available during Bridge bidding.
 */
public enum Strain {
    CLUBS, DIAMONDS, HEARTS, SPADES, NO_TRUMP;

    /**
     * Converts a bidding Strain into its corresponding playable trump {@link Suit}.
     *
     * @return The matching {@link Suit}, or {@code null} if the strain is {@code NO_TRUMP}.
     */
    public Suit toSuit() {
        switch (this) {
            case CLUBS:
                return Suit.CLUBS;
            case DIAMONDS:
                return Suit.DIAMONDS;
            case HEARTS:
                return Suit.HEARTS;
            case SPADES:
                return Suit.SPADES;
            case NO_TRUMP:
                return null;
            default:
                throw new IllegalStateException("Unknown strain value: " + this);
        }
    }
}