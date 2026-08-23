package BiddingSystem.BiddingData;

import BiddingSystem.BiddingData.Actions.ContractBid;
import BiddingSystem.BiddingData.Actions.PassAction;
import BiddingSystem.BiddingData.Actions.PlayerAction;
import BiddingSystem.Player;

import java.util.ArrayList;
import java.util.List;

public class BiddingHistory {
    //since we append to the arraylist the latestbid will always be the highest bid made, but what about passes?
    ArrayList<BidEntry> bidsMade;

    public BiddingHistory() {
        bidsMade = new ArrayList<>();
    }

    public void addBid(BidEntry bid) {
        bidsMade.add(bid);
    }

    public void clearHistory() {
        //in the case of passing out, we will clear the list so we can reuse it for the new bidding phase
        bidsMade.clear();
    }

    //number of bids made so far
    public int getSize() {
        return bidsMade.size();
    }

    public BidEntry getFirstBid() {
        return bidsMade.getFirst();
    }

    public BidEntry getLastBid() {
        return bidsMade.getLast();
    }

    public BidEntry getEntryAtIndex(int index) {
        if (index < this.getSize()) {
            // <= because index starts at 0
            return bidsMade.get(index);
        }
        //index is bigger than number of current bids
        return null;
    }

    public BidEntry getLargestBid() {
        BidEntry max = null;
        for (BidEntry entry : bidsMade){
            //only look at contractual bids, skip passes will work on doubles and redoubles when added
            if (!(entry.getAction() instanceof ContractBid)){
                //skip
                continue;
            }
            if(max ==null || !max.getAction().isBigger(entry.getAction())){
                max = entry;
            }
        }
        return max;
    }

    public boolean checkNoContractBidMade (){
        //check if there have been any contractual bids made yet
        for (BidEntry bid: bidsMade){
            if (bid.getAction() instanceof ContractBid){
                return false;
            }
        }
        //if not that means only passes have been made (no double or redouble added yet)
        return true;
    }

    public int countConsecutivePasses () {
        int count = 0;
        for (int i = bidsMade.size() - 1; i >= 0; i--) {
            //loop backwards counting # of passes
            if (bidsMade.get(i).getAction() instanceof PassAction) {
                count++;
            } else {
                //the moment we encounter an action that isn't a pass we jump out the loop.
                break;
            }
        }
        return count;
    }
}
