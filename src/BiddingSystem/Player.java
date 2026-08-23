package BiddingSystem;

import logic.Deck;
import logic.PlayerHand;
import logic.PlayerPosition;

public class Player {
    final private String username;
    final private PlayerHand playerHand;
    private final PlayerPosition position;
    public Player(String name, PlayerHand pH, PlayerPosition pos){
        this.username =name;
        this.playerHand = pH;
        this.position = pos;
        //a number between 0-3, 0 being North, 1 being East, 2 being South, 3 being West
    }

    public PlayerPosition getSeatPosition() {
        return position;
    }

    public PlayerHand getPlayerHand() {
        return playerHand;
    }

    public String getUsername(){
        return username;
    }
}
