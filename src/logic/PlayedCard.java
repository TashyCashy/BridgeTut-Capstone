package logic;

public class PlayedCard {
    private final Card card;
    private final PlayerPosition player;

    public PlayedCard (PlayerPosition player, Card card) {
        this.player = player;
        this.card = card;
    }

    public PlayerPosition getPlayer() {
        return this.player;
    }

    public Card getCard() {
        return this.card;
    }
}