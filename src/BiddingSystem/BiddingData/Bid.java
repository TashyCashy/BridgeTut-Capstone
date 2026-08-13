package BiddingSystem.BiddingData;

import logic.Suit;

public class Bid{
    private int level; //# from 1-7
    private  Suit suit; //the suit that was bid

    public Bid(int l, Suit s) {
        this.level = l;
        this.level = Math.clamp(level, 1, 7); // value cant be bigger than 7 or smaller than 1
        this.suit = s;
    }

    public int getLevel() {return level; }

    public Suit getSuit() { return suit; }

    public void setLevel(int level) { this.level = level; }

    public void setSuit(Suit suit) { this.suit = suit; }


    public int compareBid(Bid otherBid) {
        //the bid being compared to is already bigger if it's level is higher
        if (otherBid.getLevel() > this.level) {
            return -1; // this instance is smaller than the argued instance
        }
        // else compare suits, established that they are the same level
        if (this.suit.ordinal() > otherBid.getSuit().ordinal()){
            return 1;
        }
        //otherbid has higher suit value;
        return -1;
        //no need to return 0 to indicate equal bids because bids cant ever be equal.
        }

}
