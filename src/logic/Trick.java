package logic;

import java.util.LinkedHashMap;
import java.util.Map;

public class Trick {
    private Suit ledSuit;
    private final Map<PlayerPosition, Card> playedCards = new LinkedHashMap<>();
    private final PlayerPosition lead;

    public Trick(PlayerPosition lead) {
        this.lead = lead;
    }

    public PlayerPosition getLead() {
        return this.lead;
    }

    public Suit getLedSuit() {
        return this.ledSuit;
    }

    public Map<PlayerPosition, Card> getPlayedCards() {
        return this.playedCards;
    }

    /**
     * records card as played by the player
     * doesn't check whether move is allowed or not
     * @param player
     * @param card
     */
    public void recordPlay(PlayerPosition player, Card card) {
        if (playedCards.isEmpty())
            this.ledSuit = card.getSuit();
        playedCards.put(player, card);
    }

    /**
     * decides who plays first card during partiicular trick.
     * bridge rule: player on the declarer's left goes first
     * @param declarer, Konke's bidding/declarer stuff
     * @return a new. empty Trick led by the correct player
     */
    public static Trick startTrick(PlayerPosition declarer) {
        PlayerPosition leader = declarer.next();
        return new Trick(leader);
    }

    /**
     *
     * @return true when all players have played a card during this trick
     */
    public boolean done() {
        return playedCards.size() == 4;
    }

    public Trick copy(){
        Trick copy = new Trick(lead);
        //copy every entry already recorderd
        copy.playedCards.putAll(this.playedCards);
        copy.ledSuit = this.ledSuit;
        return copy;
    }
}
