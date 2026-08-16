package BiddingSystem.BiddingData.Actions;

import logic.Suit;

abstract public class PlayerAction {
    //this serves as the parent class that all the different action classes will inherit
    //Johan said that the parent class must be a subset of all children meaning the children must add implmentation to the already declared implementation in this class
    int level = 0;
    Suit suit = null;
    abstract public String getDisplayString ();

     public boolean isBigger(PlayerAction otherBid) {
         //the bid being compared to is already bigger if it's level is higher
         if (otherBid.getLevel() > this.level) {
             return false; // this instance is smaller than the argued instance
         }
         // else compare suits, after confirming that they are the same level
         //true if this bid is bigger, false if otherBid is bigger
         return this.suit.ordinal() > otherBid.getSuit().ordinal();
     }

     int getLevel(){ return level;}

    Suit getSuit() { return suit; }

}
