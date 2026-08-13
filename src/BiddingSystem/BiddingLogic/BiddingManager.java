package BiddingSystem.BiddingLogic;

import BiddingSystem.BiddingData.Actions.PlayerAction;
import BiddingSystem.BiddingData.BidEntry;
import BiddingSystem.BiddingData.BiddingHistory;
import BiddingSystem.Player;

public class BiddingManager {
    //this keeps track of turns and has the logic that will find the final bid
    private int currentSeat;
    private Player[] players;//this array will hold the players in the order of their seats so N,E,S,W
    BiddingHistory biddingHistory;

    public BiddingManager (int startingPos, Player [] plyrs){
        //the player who will do the first bid, not sure if Sonia wants us to start from North everytime as in the game's rules it is usually the person who dealt the cards
        this.currentSeat = startingPos;
        this.players = plyrs;
        this.biddingHistory = new BiddingHistory();
    }
    public void advanceTurn (){
        currentSeat = (currentSeat +1)%4;
    }
    //returns who's turn it is at the moment
    public Player getCurrentPlayer (){
        return players[currentSeat];
    }
    //after a player does some action, it must be logged and the turn advanced.
    public void ActionPlayed (PlayerAction p){
        biddingHistory.addBid(new BidEntry(p, getCurrentPlayer() ));
        advanceTurn();
    }
}
