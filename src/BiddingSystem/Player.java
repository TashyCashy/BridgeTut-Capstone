package BiddingSystem;

import logic.Deck;

public class Player {
    final private String username;
    private Deck playerDeck;
    private final int position;
    public Player(String name, Deck d, int pos){
        this.username =name;
        this.playerDeck = d;
        this.position = pos;
        //a number between 0-3, 0 being North, 1 being East, 2 being South, 3 being West
    }

    public int getSeatPosition() {
        return position;
    }

    public Deck getPlayerDeck() {
        return playerDeck;
    }

    public String getUsername(){
        return username;
    }
}
