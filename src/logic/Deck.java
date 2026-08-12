package logic;
import java.util.*;

public class Deck {
    private final List<Card> cards  = new ArrayList<>();

    public Deck() {
        for (Suit suit: Suit.values()) {
            for (Rank rank: Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }
}
