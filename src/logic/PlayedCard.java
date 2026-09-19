package logic;

/**
 * Data wrapper representing a single card played by a specific player seat during a trick.
 */
public class PlayedCard {
    private final Card card;
    private final PlayerPosition player;

    /**
     * Constructs a Recorded Card Play.
     *
     * @param player The {@link PlayerPosition} who played the card.
     * @param card   The {@link Card} played.
     */
    public PlayedCard(PlayerPosition player, Card card) {
        this.player = player;
        this.card = card;
    }

    /**
     * Gets the seat position of the player who played this card.
     *
     * @return Player's {@link PlayerPosition}.
     */
    public PlayerPosition getPlayer() {
        return this.player;
    }

    /**
     * Gets the card that was played.
     *
     * @return The {@link Card}.
     */
    public Card getCard() {
        return this.card;
    }
}