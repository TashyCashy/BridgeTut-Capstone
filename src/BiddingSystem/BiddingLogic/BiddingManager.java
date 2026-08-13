package BiddingSystem.BiddingLogic;

import BiddingSystem.Player;

public class BiddingManager {
    //this keeps track of turns and has the logic that will find the final bid
    private int currentSeat;
    private Player[] players;
    public BiddingManager (int startingPos, Player [] plyrs){
        //the player who will do the first bid, not sure if Sonia wants us to start from North everytime as in the game's rules it is usually the person who dealt the cards
        currentSeat = startingPos;
        players = plyrs;
    }
    public void advanceTurn (){
        currentSeat = (currentSeat +1)%4;
    }
}
