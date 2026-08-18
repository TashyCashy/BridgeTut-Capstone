package logic;

public enum Strain {
    CLUBS, DIAMONDS, HEARTS, SPADES, NO_TRUMP;

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
                throw new IllegalStateException();
        }
    }
}