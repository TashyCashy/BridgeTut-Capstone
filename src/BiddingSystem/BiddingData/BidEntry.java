package BiddingSystem.BiddingData;

import BiddingSystem.Player;

public class BidEntry {
    //this class makes a bid made by a player into one entry that will be used for storing and logging who did what bid
    final private Bid bid;
    final private Player player;

    public BidEntry (Bid b, Player p){
        this.bid = b;
        this.player = p;
    }


}
