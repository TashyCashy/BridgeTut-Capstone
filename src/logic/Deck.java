package logic;
import java.util.*;

public class Deck {
    private final List<Card> cards  = new ArrayList<>();

    public Deck() {
        for (Suit suit: Suit.values()) {
            for (Rank rank: Rank.values()) { // values() is a built-in method that comes with enums
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public void printDeck() {
        for (Card card: cards) {
            System.out.println(card);
        }
    }
}
