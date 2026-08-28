package BiddingSystem.BiddingLogic;

import BiddingSystem.BiddingData.Actions.ContractBid;
import BiddingSystem.BiddingData.Actions.PassAction;
import BiddingSystem.BiddingData.Actions.PlayerAction;
import BiddingSystem.BiddingData.BidEntry;
import BiddingSystem.BiddingData.BiddingHistory;
import BiddingSystem.Player;
import logic.PlayerPosition;
import logic.Strain;

public class BiddingManager {
    //this class keeps track of turns and has the logic that will find the final bid
    private PlayerPosition currentSeat;
    final private Player[] players;//this array will hold the players in the order of their seats so S, W, N, E
    final private BiddingHistory biddingHistory;
    final BiddingValidator biddingValidator;
    private Player declarer;
    private ContractBid  winningContract;
    //adding this to hodl reference to the starting position in the case of passing out, we need what it was after modifying it
    private final PlayerPosition startingPosition;
    private boolean passedOut = false;



    public BiddingManager(PlayerPosition startPos, Player[] plyrs) {
        //the player who will do the first bid, not sure if Sonia wants us to start from North everytime as in the game's rules it is usually the person who dealt the cards
        this.startingPosition = startPos;
        this.currentSeat = startPos;
        this.players = plyrs;
        this.biddingHistory = new BiddingHistory();
        this.biddingValidator = new BiddingValidator();
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
        //must also make the game still end if highest bid possible is made thart would be 7NT (already implemented as everyone would have to pass)
        if (!(biddingHistory.checkNoContractBidMade()) && biddingHistory.countConsecutivePasses() == 3) {
            //a bid must have been made , there exists a single contractual bid that was made (for this to work, this method has to be called after every player action -- since we loop backwards when checking consective passes)
            BidEntry winningEntry = biddingHistory.getLargestBid();
            //store the specifc strain that won
            Strain winningStrain = ((ContractBid) winningEntry.getAction()).getStrain();
            //as well as the action.
            winningContract = (ContractBid) winningEntry.getAction();
            declarer = biddingHistory.determineDeclarer(winningStrain, winningEntry.getPlayer());
            System.out.println("Bidding is over: The winning contract is " + winningStrain + ", at level " + winningContract.getLevel() + " and the declarer is seated at " + declarer.getSeatPosition());
            return true;

            //end the game
        }
        //created a boolean flag, that will telll the main game class, that the reason bidding is over, is because of passing out, and that will result in it calling gamereset.
        if (biddingHistory.checkNoContractBidMade() && biddingHistory.countConsecutivePasses() == 4) {
            passedOut = true;
            return true;
        }
        System.out.println("Bidding is still in ongoing!");
        return false;
    }

    public Player [] getPlayers (){
        return players;
    }

    public PlayerPosition getStartingPosition(){
        return startingPosition;
    }

    public Player getDeclarer() {
        return declarer;
    }

    public boolean isPassedOut() {
        return passedOut;
    }

    public ContractBid getWinningContract() {
        return winningContract;
    }
}
