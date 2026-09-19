package logic;
import java.util.*;

/**
 * Models a standard 52 card deck used in Bridge dealing operations.
 */
public class Deck {
    private final List<Card> cards  = new ArrayList<>();

    /**
     * Constructs and initialises a standard 52 card deck ordered by suit and rank.
     */
    public Deck() {
        for (Suit suit: Suit.values()) {
            for (Rank rank: Rank.values()) { // values() is a built-in method that comes with enums
                cards.add(new Card(suit, rank));
            }
        }
    }

    /**
     * Randomly shuffles the cards remaining in the deck.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Removes and returns the top card from the deck.
     * 
     * @return The drawn {@link Card} or {@code null} if the deck is empty.
     */
    public Card drawCard (){
        if (cards.isEmpty()){
            return null;
        }
        return cards.removeLast();
    }

    /**
     * Gets the number of cards remaining in the deck.
     * 
     * @return Integer count of remaining cards.
     */
    public int size(){
        return cards.size();
    }

    /**
     * Prints the current state of cards in the deck to standard output.
     */
    public void printDeck() {
        for (Card card: cards) {
            System.out.print(card);
        }
    }
}
