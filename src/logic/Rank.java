package logic;

public enum Rank {
    TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;

    /**
     * getting the card value
     * @return ordinal of this enumeration constant (its position in its enum declaration, where the initial constant is assigned an ordinal of zero)
     */
    public String getRankLetter() {
        switch (this) {
            case JACK: return "J";
            case QUEEN: return "Q";
            case KING: return "K";
            case ACE: return "A";
            default: return String.valueOf(ordinal()+2); 
        }
    }
}