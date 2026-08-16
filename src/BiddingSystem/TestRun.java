package BiddingSystem;


import BiddingSystem.BiddingLogic.BiddingManager;
import logic.Deck;

public class TestRun {
    Player plyr1 = new Player("Konke", new Deck(), 0);
    Player plyr2 = new Player("Tash", new Deck(), 1);
    Player plyr3 = new Player("Joye", new Deck(), 2);
    Player plyr4 = new Player("Jack", new Deck(), 3);
    Player [] plyrs = {plyr1, plyr2, plyr3, plyr4};
    BiddingManager bM = new BiddingManager(0, plyrs);
}
