package BiddingSystem;

import logic.Card;
import logic.Deck;
import logic.PlayerHand;

public class DealCards {

  public static void dealHands (Deck d, Player[] players){
       if (d.size() != 52){{
           throw new IllegalStateException("Deck must have exactly 52 cards before dealing");
       }}
       Card c;
       //round robin dealing just to have a more realer dealing feel
        for (int i = 0; i<13; i++){
            for (Player p: players){
                c = d.drawCard();
                if (c== null){
                    throw new IllegalStateException("Deck ran out of cards during deal");
                }
                p.getPlayerHand().addCard(c);
            }
        }

    }
}
