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

    //this method was added by me so we can pull from the deck and give cards to players.
    public Card drawCard (){
        if (cards.isEmpty()){
            return null;
        }
        return cards.removeLast();
    }

    public int size(){
        return cards.size();
    }

    public void printDeck() {
        for (Card card: cards) {
            System.out.print(card);
        }
    }
}
