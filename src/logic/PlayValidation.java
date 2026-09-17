package logic;
import java.util.Map;

public class PlayValidation {

    public static boolean isLegitPlay(PlayerHand hand, Card card, Trick trick) {
        if (trick.getLedSuit() == null)
            return true;
        if (hand.hasSuit(trick.getLedSuit()))
            return trick.getLedSuit() == card.getSuit();
        return true;
    }

    // evaluate the four cards in a completed trick, taking into account the led suit and any trump suit played
    public static PlayerPosition pickWinner(Trick trick, Suit trumpSuit) {
        PlayerPosition winner = null;
        Card winningCard = null;

        for (Map.Entry<PlayerPosition, Card> entry: trick.getPlayedCards().entrySet()) {
            PlayerPosition player = entry.getKey();
            Card card = entry.getValue();
            if (winningCard == null) {
                winner = player;
                winningCard = card;
            }
            else {
                if (wins(card, winningCard, trick.getLedSuit(), trumpSuit)) {
                    winner = player;
                    winningCard = card;
                }
            }
        }
        return winner;
    }

    private static boolean wins(Card newCard, Card winningCard, Suit ledSuit, Suit trumpSuit) {
        if (trumpSuit != null) {
            // if the newCard is a trump card and the current winningCard is not a trump, newCard wins
            if (newCard.getSuit() == trumpSuit && winningCard.getSuit() != trumpSuit) 
                return true;
            // if the winningCard is a trump card and newCard is not, the newCard loses
            if (newCard.getSuit() != trumpSuit && winningCard.getSuit() == trumpSuit) 
                return false;
            // if they are both trump cards, compare their ranks
            if (newCard.getSuit() == trumpSuit && winningCard.getSuit() == trumpSuit) {
                return newCard.compareRank(winningCard) > 0;
            }
        }

        // what if none of the cards are trump cards?
        // a card that doesn't follow the led suit loses
        if (newCard.getSuit() != ledSuit)
            return false;
        
        // if the winningCard doesn't follow the led suit but the newCard does, newCard wins
        if (winningCard.getSuit() != ledSuit)
            return true;
        
        // if both are led suits, compare their ranks
        return newCard.compareRank(winningCard) > 0;
    }
}