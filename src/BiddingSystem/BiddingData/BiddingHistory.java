package BiddingSystem.BiddingData;

import BiddingSystem.Player;

import java.util.ArrayList;
import java.util.List;

public class BiddingHistory {
    //since we append to the arraylist the latestbid will always be the highest bid made, but what about passes?
    ArrayList<BidEntry> bidsMade;
    public BiddingHistory (){
        bidsMade = new ArrayList<>();
    }

    public void addBid (BidEntry bid){
        bidsMade.add(bid);
    }

    public void clearHistory (){
        //in the case of passing out, we will clear the list so we can reuse it for the new bidding phase
        bidsMade.clear();
    }

    //number of bids made so far
    public int getSize (){
        return bidsMade.size();
    }

    public BidEntry getFirstBid (){
        return bidsMade.getFirst();
    }

    public BidEntry getLastBid (){
        return bidsMade.getLast();
    }

    public BidEntry getEntryAtIndex (int index){
        if (index < this.getSize()){
            // <= because index starts at 0
            return bidsMade.get(index);
        }
        //index is bigger than number of current bids
        return null;
    }


}
