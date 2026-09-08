package BiddingSystem.BiddingLogic;

import BiddingSystem.BiddingData.Actions.*;
import BiddingSystem.BiddingData.BidEntry;
import BiddingSystem.BiddingData.BiddingHistory;
import BiddingSystem.BiddingData.DoublingState;
import BiddingSystem.Player;
import logic.PlayerPosition;

public class BiddingValidator {
    //BiddingHistory biddingHistory;
    public BiddingValidator (){
    }

   public boolean validateBid (PlayerAction pA, BiddingHistory bH, Player actingPlayer){
        if (bH.checkNoContractBidMade()){
            //any bid is valid, since only passes have been made, will add a check for passing out or 3 passes later.
            return true;
        }

            BidEntry highestBid = bH.getLargestBid();
            if (pA instanceof ContractBid cB) {
                //if proposed action is bigger than the current largest bid return true
                //if not return false
                return cB.isBigger(highestBid.getAction());
            }

       if (pA instanceof DoubleAction) {
           if (bH.getCurrentDoublingState() != DoublingState.UNDOUBLED) return false;
           BidEntry highest = bH.getLargestBid();
           if (highest == null) return false;

           PlayerPosition bidderPos = highestBid.getPlayer().getSeatPosition();
           PlayerPosition doublerPos = actingPlayer.getSeatPosition();
           boolean sameSide = (doublerPos == bidderPos) || (doublerPos == bidderPos.partner());
           if (sameSide) return false; // can't double your own partnership's bid

           return true;
       }
       if (pA instanceof RedoubleAction) {
           if (bH.getCurrentDoublingState() != DoublingState.DOUBLED) return false;
           BidEntry highest = bH.getLargestBid();
           if (highest == null) return false; // shouldn't happen if DOUBLED, but guard anyway

           PlayerPosition bidderPos = highestBid.getPlayer().getSeatPosition();
           PlayerPosition redoublerPos = actingPlayer.getSeatPosition();
           boolean sameSide = (redoublerPos == bidderPos) || (redoublerPos == bidderPos.partner());
           if (!sameSide) return false; // only the doubled side can redouble

           return true;
       }
            //again double and redouble have not been implemented yet.
            else {
                //if proposed bid is a pass it is valid, the only thing needed to be added now it the double and redouble feature where i will check if the player who played before is an enemy or team member
                return true;
            }




        }
}
