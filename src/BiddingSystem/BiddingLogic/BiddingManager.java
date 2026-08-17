package BiddingSystem.BiddingLogic;

import BiddingSystem.BiddingData.Actions.PlayerAction;
import BiddingSystem.BiddingData.BidEntry;
import BiddingSystem.BiddingData.BiddingHistory;
import BiddingSystem.Player;

public class BiddingManager {
    //this keeps track of turns and has the logic that will find the final bid
    private int currentSeat;
    final private Player[] players;//this array will hold the players in the order of their seats so N,E,S,W
    final private BiddingHistory biddingHistory;
    final BiddingValidator biddingValidator;

    public BiddingManager (int startingPos, Player [] plyrs){
        //the player who will do the first bid, not sure if Sonia wants us to start from North everytime as in the game's rules it is usually the person who dealt the cards
        this.currentSeat = startingPos;
        this.players = plyrs;
        this.biddingHistory = new BiddingHistory();
        this.biddingValidator = new BiddingValidator();
    }
    public void advanceTurn (){
        currentSeat = (currentSeat +1)%4;
    }
    //returns whos turn it is at the moment
    public Player getCurrentPlayer (){
        return players[currentSeat];
    }
    //after a player does some action, it must be logged and the turn advanced.
    public boolean ActionPlayed (PlayerAction p){
        if (biddingValidator.validateBid(p, biddingHistory)){
            biddingHistory.addBid(new BidEntry(p, getCurrentPlayer() ));
            advanceTurn();
            return true;
        }
        return false;
    }
}
