package logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the collection of cards held by a player at a specific seat position,
 * handling sorting, suit verification, and card additions/removals.
 */
public class PlayerHand {
    private final PlayerPosition position;
    private final List<Card> hand = new ArrayList<>();

    /**
     * Constructs an empty hand for a given seat position.
     *
     * @param position The {@link PlayerPosition} seat associated with this hand.
     */
    public PlayerHand(PlayerPosition position) {
        this.position = position;
    }

    /**
     * Gets the seat position associated with this hand.
     *
     * @return The {@link PlayerPosition}.
     */
    public PlayerPosition getPlayerPosition() {
        return this.position;
    }

    /**
     * Gets the list of cards currently held in this hand.
     *
     * @return List of {@link Card} objects.
     */
    public List<Card> getHand() {
        return this.hand;
    }

    /**
     * Adds a card to the hand and automatically resorts the hand.
     *
     * @param card The {@link Card} to add.
     */
    public void addCard(Card card) {
        hand.add(card);
        sortHand();
    }

    /**
     * Removes a card from the hand when played.
     *
     * @param card The {@link Card} to remove.
     */
    public void removeCard(Card card) {
        hand.remove(card);
    }

    /**
     * Gets the count of cards remaining in the hand.
     *
     * @return Number of cards in hand.
     */
    public int size() {
        return hand.size();
    }

    /**
     * Checks if the hand contains at least one card matching the given suit.
     * Useful for checking follow-suit rules.
     *
     * @param suit The {@link Suit} to search for.
     * @return {@code true} if the hand contains the suit; {@code false} otherwise.
     */
    public boolean hasSuit(Suit suit) {
        for (Card card : hand) {
            if (card.getSuit() == suit) {
                return true;
            }
        }
        return false;
    }

    /**
     * Internal comparator for sorting cards by suit first, then by rank.
     */
    private boolean isBigger(Card one, Card two) {
        if (one.getSuit() != two.getSuit()) {
            return one.getSuit().ordinal() > two.getSuit().ordinal();
        }
        return one.getRank().ordinal() > two.getRank().ordinal();
    }

    /**
     * Sorts the hand using insertion sort based on suit and rank precedence.
     */
    private void sortHand() {
        for (int i = 1; i < hand.size(); i++) {
            Card card = hand.get(i);
            int j = i - 1;
            while (j >= 0 && isBigger(hand.get(j), card)) {
                hand.set(j + 1, hand.get(j));
                j--;
            }
            hand.set(j + 1, card);
        }
    }

    /**
     * Prints the current hand contents to standard output.
     */
    public void printHand() {
        for (Card c : hand) {
            System.out.print(c + ", ");
        }
    }

    /**
     * Removes all cards from the hand.
     */
    public void clearHand() {
        hand.clear();
    }
}