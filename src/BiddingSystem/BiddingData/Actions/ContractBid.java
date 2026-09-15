package BiddingSystem.BiddingData.Actions;

import BiddingSystem.BiddingData.DoublingState;
import logic.Strain;
import logic.Suit;

public class ContractBid extends PlayerAction {
    private int level; //# from 1-7
    private Strain strain; //the suit that was bid /strain

    public ContractBid(int l, Strain s) {
        this.level = l;
        //this.level = Math.clamp(level, 1, 7); // value cant be bigger than 7 or smaller than 1
        this.strain = s;
    }

    public int getLevel() {return level; }

    public Strain getStrain() { return strain; }

    public void setLevel(int level) { this.level = level; }

    public void setStrain(Strain s) { this.strain = s; }



    public boolean isBigger(PlayerAction otherBid) {
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
        return this.strain.ordinal() > otherBid.getStrain().ordinal();
    }

    @Override
    public String getDisplayString() {
        return "ContractBid-> " + "Level: " +level + ", Suit: " + strain;
    }
}
