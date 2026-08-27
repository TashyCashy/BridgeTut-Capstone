package BiddingSystem;

import logic.Deck;
import logic.PlayerHand;
import logic.PlayerPosition;

public class BiddingSimulation {
    public static void main(String[] args) {
        Deck deck = new Deck();
        deck.shuffle();
        // Seats: 0=North, 1=East, 2=South, 3=West
        Player north = new Player("North", new PlayerHand(PlayerPosition.NORTH), PlayerPosition.NORTH);
        Player east  = new Player("East",  new PlayerHand(PlayerPosition.EAST), PlayerPosition.EAST);
        Player south = new Player("South", new PlayerHand(PlayerPosition.SOUTH), PlayerPosition.SOUTH);
        Player west  = new Player("West", new PlayerHand(PlayerPosition.WEST), PlayerPosition.WEST);
        Player[] players = { south, west, east, north };
        DealCards.dealHands(deck, players);
        for (Player p: players){
            p.getPlayerHand().printHand();
            System.out.println();
        }
    }

}
