package BiddingSystem.BiddingData.Actions;

import logic.Suit;

public class PassAction extends PlayerAction {
    private final int level = 0;
    private final Suit suit = null;

    public PassAction() {
    }
    @Override
    public String getDisplayString() {
        return "Pass";
    }

    public boolean isBigger(PlayerAction otherBid) {
        //the bid being compared to is already bigger if it's level is higher
        if (otherBid.getLevel() > this.level) {
            return false; // this instance is smaller than the argued instance
        }
        // else compare suits, after confirming that they are the same level
        //true if this bid is bigger, false if otherBid is bigger
        return this.suit.ordinal() > otherBid.getSuit().ordinal();
    }
}
