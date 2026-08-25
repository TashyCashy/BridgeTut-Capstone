package BiddingSystem;

import logic.Deck;
import logic.PlayerHand;

public class DealCards {

   static void dealHand(Deck d, Player[] players){
        for (int i = 0; i<13; i++){
            for (Player p: players){
                p.getPlayerHand().addCard(d.drawCard());
            }
        }

    }
}
