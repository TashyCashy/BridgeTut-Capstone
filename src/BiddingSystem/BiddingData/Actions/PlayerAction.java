package BiddingSystem.BiddingData.Actions;

import logic.Strain;
import logic.Suit;

abstract public class PlayerAction {
    //this serves as the parent class that all the different action classes will inherit
    //Johan said that the parent class must be a subset of all children meaning the children must add implmentation to the already declared implementation in this class
    int level = 0;
    Strain strain = null;
    abstract public String getDisplayString ();

     /*public boolean isBigger(PlayerAction otherBid) {
         //the bid being compared to is already bigger if it's level is higher
         //before checking if which is bigger between the two if other bid is an instance of passaction no need to check
         if (!(otherBid instanceof ContractBid)){
             //for now since double and redouble not yet added, contractbid will always be bigger than a pass
             return true;
         }
         if (otherBid.getLevel() > this.level) {
             return false; // this instance is smaller than the argued instance
         }
         else if (otherBid.getLevel() < this.level){
             return true;
             //previously didnt check if this bid is bigger than the other one, just automatically went to comparing suits
         }
         // else compare suits, after confirming that they are the same level
         //true if this bid is bigger, false if otherBid is bigger
         return this.suit.ordinal() > otherBid.getSuit().ordinal();
     }*/

     public int getLevel(){ return level;}

    public Strain getStrain() { return strain; }

}
