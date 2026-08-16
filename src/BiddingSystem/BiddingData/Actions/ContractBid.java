package BiddingSystem.BiddingData.Actions;

import logic.Suit;

public class ContractBid extends PlayerAction {
    private int level; //# from 1-7
    private Suit suit; //the suit that was bid

    public ContractBid(int l, Suit s) {
        this.level = l;
        //this.level = Math.clamp(level, 1, 7); // value cant be bigger than 7 or smaller than 1
        this.suit = s;
    }

    public int getLevel() {return level; }

    public Suit getSuit() { return suit; }

    public void setLevel(int level) { this.level = level; }

    public void setSuit(Suit suit) { this.suit = suit; }


    public boolean isBigger(PlayerAction otherBid) {
        //the bid being compared to is already bigger if it's level is higher
        if (otherBid.getLevel() > this.level) {
            return false; // this instance is smaller than the argued instance
        }
        // else compare suits, after confirming that they are the same level
        //true if this bid is bigger, false if otherBid is bigger
        return this.suit.ordinal() > otherBid.getSuit().ordinal();
    }

    @Override
    public String getDisplayString() {
        return "ContractBid";
    }
}
