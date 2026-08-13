package BiddingSystem;

import logic.Deck;

public class Player {
    String username;
    Deck playerDeck;
    int position;
    public Player(String name, Deck d, int pos){
        this.username =name;
        this.playerDeck = d;
        this.position = pos;
        //a number between 0-3, 0 being North, 1 being East, 2 being South, 3 being West
    }
}
