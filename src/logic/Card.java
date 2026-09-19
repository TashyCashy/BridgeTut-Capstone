package logic;

/**
 * Represents an immutable playing card with specific {@link Suit} and {@link Rank}
 * Used throughout the Bridge engine to model hands, tricks and card plays.
 */
public class Card {
    private final Suit suit;
    private final Rank rank;

    /**
     * Constructs a Card with the specified suit and rank.
     * 
     * @param suit The suit of the card.
     * @param rank The rank of the card.
     */
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    /** 
     * Gets the suit of this card.
     * 
     * @return The card's {@link Suit}
     */
    public Suit getSuit() {
        return this.suit;
    }

    /** 
     * Gets the rank of this card.
     * 
     * @return The card's {@link Rank}
     */
    public Rank getRank() {
        return this.rank;
    }

    /**    
     * Returns 2-character string representation of the card (e.g., "JH" for Jack of Hearts)
     * 
     * @return String code of the card.
     */
    @Override 
    public String toString() {
        return rank.getRankLetter() + suit.getSuitLetter(); // for example, "JH" would read as Jack of Hearts
    }

    /**
     * Compares this card's rank to another card's rank, all while ignoring the suit.
     * 
     * @param other The card to compare against.
     * @return A negative interer, zero or a positive integer as this card's rank is 
     * is less than, equal to or greater than the specified card's rank.
     */
    public int compareRank(Card other) {
        return this.rank.compareTo(other.rank);
    }

    /**    
     * Checks equality based on matching suit and rank.
     * 
     * @param object The object to compare.
     * @return {@code true} if the object is a Card with identical suit and rank; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Card)) {
            return false;
        }
        Card card = (Card) object;
        return suit == card.suit && rank == card.rank;
    }

    /**    k
     * Generates a unique integer hash code for this card based on suit and rank ordinal values.
     * 
     * @return Hash code integer.
     */
    @Override 
    public int hashCode() {
        return suit.ordinal()*13 + rank.ordinal();
    }
}