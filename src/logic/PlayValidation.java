package logic;

import java.util.Map;

/**
 * Utility class executing official Bridge rules for card play legality and trick winner evaluation.
 */
public class PlayValidation {

    /**
     * Validates whether playing a specific card from a hand complies with the follow-suit rule.
     *
     * @param hand  The {@link PlayerHand} containing available cards.
     * @param card  The {@link Card} attempting to be played.
     * @param trick The active {@link Trick} being played into.
     * @return {@code true} if the move follows suit or if the player cannot follow suit; {@code false} if illegal.
     */
    public static boolean isLegitPlay(PlayerHand hand, Card card, Trick trick) {
        if (trick.getLedSuit() == null)
            return true;
        if (hand.hasSuit(trick.getLedSuit()))
            return trick.getLedSuit() == card.getSuit();
        return true;
    }

    /**
     * Evaluates all four played cards in a completed trick to determine the winning seat position.
     * Correctly prioritizes trump suit cards over high cards of the led suit.
     *
     * @param trick     The completed {@link Trick} containing 4 played cards.
     * @param trumpSuit The active trump {@link Suit} (or {@code null} for No-Trump contracts).
     * @return The {@link PlayerPosition} seat that won the trick.
     */
    public static PlayerPosition pickWinner(Trick trick, Suit trumpSuit) {
        PlayerPosition winner = null;
        Card winningCard = null;

        for (Map.Entry<PlayerPosition, Card> entry : trick.getPlayedCards().entrySet()) {
            PlayerPosition player = entry.getKey();
            Card card = entry.getValue();
            if (winningCard == null) {
                winner = player;
                winningCard = card;
            } else {
                if (wins(card, winningCard, trick.getLedSuit(), trumpSuit)) {
                    winner = player;
                    winningCard = card;
                }
            }
        }
        return winner;
    }

    /**
     * Internal comparison helper determining if a new card beats the current winning card.
     */
    private static boolean wins(Card newCard, Card winningCard, Suit ledSuit, Suit trumpSuit) {
        if (trumpSuit != null) {
            // If newCard is a trump and winningCard is not, newCard wins
            if (newCard.getSuit() == trumpSuit && winningCard.getSuit() != trumpSuit) 
                return true;
            // If winningCard is a trump and newCard is not, newCard loses
            if (newCard.getSuit() != trumpSuit && winningCard.getSuit() == trumpSuit) 
                return false;
            // If both are trump cards, compare ranks directly
            if (newCard.getSuit() == trumpSuit && winningCard.getSuit() == trumpSuit) {
                return newCard.compareRank(winningCard) > 0;
            }
        }

        // Non-trump card evaluation (only reached when neither card is a trump)
        if (newCard.getSuit() != ledSuit)
            return false;
        
        if (winningCard.getSuit() != ledSuit)
            return true;
        
        return newCard.compareRank(winningCard) > 0;
    }
}