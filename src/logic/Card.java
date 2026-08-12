package logic;

public class Card {
    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return this.suit;
    }

    public Rank getRank() {
        return this.rank;
    }

    @Override 
    public String toString() {
        return rank.getRankLetter() + suit.getSuitLetter(); // for example, "JH" would read as Jack of Hearts
    }

    /**
     * Compares this card's rank to another card's rank, all while ignoring the suit.
     */
    public int compareRank(Card other) {
        return this.rank.compareTo(other.rank);
    }

    @Override
    // come on tash, first year stuff...
    public boolean equals(Object object) {
        if (!(object instanceof Card)) {
            return false;
        }
        Card card = (Card) object;
        return suit == card.suit && rank == card.rank;
    }

    @Override 
    public int hashCode() {
        return suit.ordinal()*13 + rank.ordinal();
    }
}