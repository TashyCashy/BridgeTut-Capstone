package BiddingSystem.BiddingData;

import BiddingSystem.BiddingData.Actions.PlayerAction;
import BiddingSystem.Player;

public class BidEntry {
    //this class makes a bid made by a player into one entry that will be used for storing and logging who did what bid
    final private PlayerAction action;
    final private Player player;

    public BidEntry (PlayerAction a, Player p){
        this.action = a;
        this.player = p;
    }

    public PlayerAction getAction() {
        return action;
    }

    public Player getPlayer() {
        return player;
    }
}
