package BiddingSystem;

import logic.Deck;

public class Player {
    String username;
    Deck playerDeck;
    public Player(String name, Deck d){
        this.username =name;
        playerDeck = d;
    }
}
