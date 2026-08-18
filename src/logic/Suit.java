package logic;

public enum Suit {
    CLUBS, DIAMONDS, HEARTS, SPADES;

    /**
     * helps to determine "strength" of card for trick determination
     * @return suit associated with card
     */
    public String getSuitLetter() {
        switch (this) {
            case CLUBS: return "C";
            case DIAMONDS: return "D";
            case HEARTS: return "H";
            case SPADES: return "S";
            default: throw new IllegalStateException();
        }
    } 
}