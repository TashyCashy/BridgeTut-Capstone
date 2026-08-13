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

    void addBid (BidEntry bid){
        bidsMade.add(bid);
    }

    void clearHistory (){
        //in the case of passing out, we will clear the list so we can reuse it for the new bidding phase
        bidsMade.clear();
    }


}
