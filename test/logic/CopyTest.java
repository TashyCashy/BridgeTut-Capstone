package logic;

import BiddingSystem.DealCards;
import BiddingSystem.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CopyTest {

    @Test
    void copiedGameStateIsUnaffectedByPlayOnTheOriginal() {
        Deck deck = new Deck();
        deck.shuffle();
        Player p1 = new Player("P1", new PlayerHand(PlayerPosition.SOUTH), PlayerPosition.SOUTH);
        Player p2 = new Player("P2", new PlayerHand(PlayerPosition.WEST), PlayerPosition.WEST);
        Player p3 = new Player("P3", new PlayerHand(PlayerPosition.NORTH), PlayerPosition.NORTH);
        Player p4 = new Player("P4", new PlayerHand(PlayerPosition.EAST), PlayerPosition.EAST);
        Player[] players = { p1, p2, p3, p4 };
        DealCards.dealHands(deck, players);

        GameState gs = new GameState(p1.getSeatPosition(), Suit.SPADES);
        for (Player player : players) {
            gs.dealHand(player.getSeatPosition(), player.getPlayerHand().getHand());
        }

        // snapshot before any play happens
        GameState gsCopy = gs.copy();
        assertEquals(0, gs.getCompletedTricks().size(), "no tricks played yet");
        for (Player player : players) {
            assertEquals(13, gsCopy.getHand(player.getSeatPosition()).getHand().size(),
                    "Copy should start with a full 13-card hand for every seat");
        }

        // play one full trick on the original
        for (int i = 0; i < 4; i++) {
            PlayerPosition mover = gs.getCurrentPlayerTurn();
            Card legalCard = pickLegalCard(gs, mover);
            boolean accepted = gs.playCard(mover, legalCard);
            assertTrue(accepted, "A card chosen because it's legal should always be accepted");
        }

        // the original should show the effects of that trick...
        assertEquals(1, gs.getCompletedTricks().size(), "The original should have completed one trick");
        for (Player player : players) {
            assertEquals(12, gs.getHand(player.getSeatPosition()).getHand().size(),
                    "Every seat played  one card in that trick, on original");
        }

        // while the copy, taken before the trick was played, must be untouched
        assertEquals(0, gsCopy.getCompletedTricks().size(), "The copy should be unaffected by play on the original");
        for (Player player : players) {
            assertEquals(13, gsCopy.getHand(player.getSeatPosition()).getHand().size(),
                    "The copy's hands should still be full - playing on the original must not leak into the copy");
        }
    }

    // walks a player's hand and returns the first card that's currently legal to play
    private static Card pickLegalCard(GameState game, PlayerPosition player) {
        PlayerHand hand = game.getHand(player);
        Trick trick = game.getCurrentTrick();
        for (Card card : hand.getHand()) {
            if (PlayValidation.isLegitPlay(hand, card, trick)) {
                return card;
            }
        }
        throw new IllegalStateException("No legal card found - shouldn't happen if isLegitPlay is correct");
    }
}
