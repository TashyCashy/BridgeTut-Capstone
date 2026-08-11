public class DummyCard {
    enum Suit  {
        CLUBS (0, "BLACK"),
        DIAMONDS (1, "RED"),
        HEARTS  (2, "RED"),
        SPADES (3, "BLACK");

        private final int value;
        private final String color;
        Suit (int v, String clr){
           value = v;
           color = clr;
        }

        public int getValue() {
            return value;
        }

        public String getColor() {
            return color;
        }
    }
    enum Rank {
        TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"),
        SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"),
        TEN(10, "10"), JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K"), ACE(14, "A");

        private final int val;
        private final String symbol;

        Rank (int v, String sym){
            val = v;
            symbol = sym;
        }

        public int getVal() {
            return val;
        }

        public String getSymbol() {
            return symbol;
        }
    }
    private Suit suit;
    private Rank rank;
    private
     DummyCard (Suit s, Rank r){
        this.suit = s;
        this.rank = r;
    }

    int compareCard(DummyCard card){
        //return 0 if equal
        //return -1 if argued card is bigger than this onw
        //return 1 if argued card is smaller than this one
        if (this.suit.getValue()> card.suit.getValue()){
            return 1;
        }
        else if (this.suit.getValue()< card.suit.getValue()){
            return -1;
        }
        return 0; //equal.
    }

    int compareSuit(DummyCard card){

    }

    int compareRank

}
