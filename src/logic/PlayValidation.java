package logic;

public class PlayValidation {

    public static boolean isLegitPlay(PlayerHand hand, Card card, Trick trick) {
        if (trick.getLedSuit() == null)
            return true;
        if (hand.hasSuit(trick.getLedSuit()))
            return trick.getLedSuit() == card.getSuit();
        return true;
    }

    // for a later issue...
    // public static PlayerPosition pickWinner(Trick trick, Suit trumpSuit) {}
}