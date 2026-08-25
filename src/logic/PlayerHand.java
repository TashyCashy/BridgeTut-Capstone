package logic;

import java.util.ArrayList;
import java.util.List;

public class PlayerHand {
    private final PlayerPosition position;
    private final List<Card> hand = new ArrayList<>();

    public PlayerHand(PlayerPosition position) {
        this.position = position;
    }

    public PlayerPosition getPlayerPosition() {
        return this.position;
    }

    public List<Card> getHand() {
        return this.hand;
    }

    public void addCard(Card card) {
        hand.add(card);
        sortHand();
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public int size() {
        return hand.size();
    }

    /**
     *
     * @param suit
     * @return true if player has cards of the given suit
     */
    public boolean hasSuit(Suit suit) {
        for (Card card : hand) {
            if (card.getSuit() == suit) {
                return true;
            }
        }
        return false;
    }

    private boolean isBigger(Card one, Card two) {
        if (one.getSuit() != two.getSuit()) {
            return one.getSuit().ordinal() > two.getSuit().ordinal();
        }
        return one.getRank().ordinal() > two.getRank().ordinal();
    }

    private void sortHand() {
        for (int i = 1; i<hand.size(); i++) {
            Card card = hand.get(i);
            int j = i-1;
            while (j >= 0 && isBigger(hand.get(j), card)) {
                hand.set(j+1, hand.get(j));
                j--;
            }
            hand.set(j+1, card);
        }
    }

    public void printHand(){
        for (Card c: hand){
            System.out.print(c + ", ");
        }
    }
}
