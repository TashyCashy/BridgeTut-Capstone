package BiddingSystem.BiddingLogic;

import BiddingSystem.BiddingData.Actions.ContractBid;
import BiddingSystem.BiddingData.Actions.PassAction;
import BiddingSystem.BiddingData.Actions.PlayerAction;
import BiddingSystem.BiddingData.BidEntry;
import BiddingSystem.BiddingData.BiddingHistory;

public class BiddingValidator {
    //BiddingHistory biddingHistory;
    public BiddingValidator (){
    }

   public boolean validateBid (PlayerAction pA, BiddingHistory bH){
        if (bH.checkNoContractBidMade()){
            //any bid is valid, since only passes have been made, will add a check for passing out or 3 passes later.
            return true;
        }
        else {
            BidEntry highestBid = bH.getLargestBid();
            if (pA instanceof ContractBid cB) {
                //if proposed action is bigger than the current largest bid return true
                //if not return false
                return cB.isBigger(highestBid.getAction());
            }
            //again double and redouble have not been implemented yet.
            else {
                //if proposed bid is a pass it is valid, the only thing needed to be added now it the double and redouble feature where i will check if the player who played before is an enemy or team member
                return true;
            }


        }

        }
}
