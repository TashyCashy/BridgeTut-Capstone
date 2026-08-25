package BiddingSystem;

import logic.Deck;
import logic.PlayerPosition;

public class BiddingSimulation {
    public static void main(String[] args) {
        Deck deck = new Deck();
        deck.shuffle();

        // Seats: 0=North, 1=East, 2=South, 3=West
        Player north = new Player("North", null, PlayerPosition.NORTH);
        Player east  = new Player("East",  null, PlayerPosition.EAST);
        Player south = new Player("South", null, PlayerPosition.SOUTH);
        Player west  = new Player("West", null, PlayerPosition.WEST);
        Player[] players = { south, west, east, north };
    }

}
