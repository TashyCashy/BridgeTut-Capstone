package BiddingSystem.BiddingData.Actions;

 abstract public class PlayerAction {
    //this serves as the parent class that all the different action classes will inherit
    //Johan said that the parent class must be a subset of all children meaning the children must add implmentation to the already declared implementation in this class

    abstract public String getDisplayString ();

}
