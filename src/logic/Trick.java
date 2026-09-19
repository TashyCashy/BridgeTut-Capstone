package logic;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a single trick during the play phase of a Bridge hand, tracking the opening lead,
 * led suit, and card plays from all four player positions.
 */
public class Trick {   
    private Suit ledSuit;
    private final Map<PlayerPosition, Card> playedCards = new LinkedHashMap<>();
    private final PlayerPosition lead;

    /**
     * Constructs a new trick starting with the specified lead player.
     *
     * @param lead The {@link PlayerPosition} seat of the opening leader for this trick.
     */
    public Trick(PlayerPosition lead) {
        this.lead = lead;
    }

    /**
     * Gets the seat position of the player who led the trick.
     *
     * @return Opening leader's {@link PlayerPosition}.
     */
    public PlayerPosition getLead() {
        return this.lead;
    }

    /**
     * Gets the suit led by the first player in this trick.
     *
     * @return The led {@link Suit}, or {@code null} if no cards have been played yet.
     */
    public Suit getLedSuit() {
        return this.ledSuit;
    }

    /**
     * Gets the map of cards played in this trick preserved in play order.
     *
     * @return Map linking each {@link PlayerPosition} seat to its played {@link Card}.
     */
    public Map<PlayerPosition, Card> getPlayedCards() {
        return this.playedCards;
    }

    /**
     * Records a card play for a given player seat. Automatically sets the trick's
     * led suit if this is the first card played in the trick.
     *
     * @param player The {@link PlayerPosition} seat playing the card.
     * @param card   The {@link Card} being played.
     */
    public void recordPlay(PlayerPosition player, Card card) {
        if (playedCards.isEmpty())
            this.ledSuit = card.getSuit();
        playedCards.put(player, card);
    }

    /**
     * Factory method creating the opening trick of a hand led by the player sitting to the left of declarer.
     *
     * @param declarer The {@link PlayerPosition} seat of the declarer.
     * @return A new, empty {@link Trick} starting with the opening leader.
     */
    public static Trick startTrick(PlayerPosition declarer) {
        PlayerPosition leader = declarer.next();
        return new Trick(leader);
    }

    /**
     * Checks if all four players have played a card into this trick.
     *
     * @return {@code true} if 4 cards have been recorded; {@code false} otherwise.
     */
    public boolean done() {
        return playedCards.size() == 4;
    }
}