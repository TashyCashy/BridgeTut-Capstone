package logic;

public enum Suit {
    CLUBS, DIAMONDS, HEARTS, SPADES;

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