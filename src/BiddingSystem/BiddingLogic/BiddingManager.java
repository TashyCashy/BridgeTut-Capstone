package BiddingSystem.BiddingLogic;

import BiddingSystem.BiddingData.Actions.PassAction;
import BiddingSystem.BiddingData.Actions.PlayerAction;
import BiddingSystem.BiddingData.BidEntry;
import BiddingSystem.BiddingData.BiddingHistory;
import BiddingSystem.Player;
import logic.Deck;
import logic.PlayerPosition;
import logic.Suit;

public class BiddingManager {
    //this class keeps track of turns and has the logic that will find the final bid
    private PlayerPosition currentSeat;
    Deck deck;
    final private Player[] players;//this array will hold the players in the order of their seats so S, W, N, E
    final private BiddingHistory biddingHistory;
    final BiddingValidator biddingValidator;
    public BidEntry winningBid;


    public BiddingManager(PlayerPosition startingPos, Player[] plyrs, Deck d) {
        //the player who will do the first bid, not sure if Sonia wants us to start from North everytime as in the game's rules it is usually the person who dealt the cards
        this.currentSeat = startingPos;
        this.players = plyrs;
        this.biddingHistory = new BiddingHistory();
        this.biddingValidator = new BiddingValidator();
        this.deck = d;
    }

    public void advanceTurn() {
        currentSeat = currentSeat.next();
    }

    //returns whos turn it is at the moment
    public Player getCurrentPlayer() {
        return players[currentSeat.ordinal()];
    }

    //after a player does some action, it must be logged and the turn advanced.
    public boolean ActionPlayed(PlayerAction p) {
        if (biddingValidator.validateBid(p, biddingHistory)) {
            biddingHistory.addBid(new BidEntry(p, getCurrentPlayer()));
            advanceTurn();
            return true;
        }
        return false;
    }

    public boolean checkBiddingOver() {
        //this method will check if the bidding phase has ended either by passing out(4 initial passes) or 3 consecutive passes (ends the biddinng)
        //must also make the game still end if highest bid possible is made thart would be 7NT
        if (!(biddingHistory.checkNoContractBidMade()) && biddingHistory.countConsecutivePasses() == 3) {
            //a bid must have been made , there exists a single contractual bid that was made (for this to work, this method has to be called after every player action -- since we loop backwards when checking consective passes)
            //this is the trump suit
            winningBid = biddingHistory.getLargestBid();
            return true;
            //end the game
        }
        if (biddingHistory.checkNoContractBidMade() && biddingHistory.countConsecutivePasses() == 4) {
            //passing out case
            //restart the game
            //new deck
            //new hands for the players.
            //all first
            return true;

        }
        return false;

    }
}
