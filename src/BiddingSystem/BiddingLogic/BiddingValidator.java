package BiddingSystem.BiddingLogic;

import BiddingSystem.BiddingData.Actions.ContractBid;
import BiddingSystem.BiddingData.Actions.PassAction;
import BiddingSystem.BiddingData.Actions.PlayerAction;
import BiddingSystem.BiddingData.BidEntry;
import BiddingSystem.BiddingData.BiddingHistory;

public class BiddingValidator {
    BiddingHistory biddingHistory;
    public BiddingValidator (BiddingHistory bH){
        this.biddingHistory = bH;
    }

    boolean validateBid (){
       BidEntry latestBid = biddingHistory.getLastBid();
        if (latestBid != null){
            PlayerAction latestAction = biddingHistory.getLastBid().getAction();
            //no need to check if it is an instance of playeraction because the array of biddinghistory onluy accepts playeraction objects
            if (latestAction instanceof PassAction){
                //is a pass no need to check any further can move to next player
                return true;
            }
            else if (latestAction instanceof ContractBid){
                //check if this bid is greater than the current maximum bid.
            }
            //
        }
        return false;
        }
    //checks if bids are legal
}
