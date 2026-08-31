package BiddingSystem.BiddingLogic;

import BiddingSystem.BiddingData.BiddingHistory;
import BiddingSystem.DealCards;
import BiddingSystem.Player;
import logic.Deck;
import logic.PlayerPosition;

public class GameReset {
   public static BiddingManager resetGame (BiddingManager manager){
            Player [] players = manager.getPlayers();
            for (Player p : players){
                p.getPlayerHand().clearHand();
            }
            //maybe should post the game history to the database beforehand
            Deck deck = new Deck();
            deck.shuffle();
            DealCards.dealHands(deck, players);

            return new BiddingManager(manager.getStartingPosition().next(), players);
   }
}
