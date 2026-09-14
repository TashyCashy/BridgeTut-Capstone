package test.BiddingSystem;

import BiddingSystem.PlayingGateway;
import logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayingGatewayTest {

    private GameState gameState;
    private PlayingGateway playingGateway;

    @BeforeEach
    void setUp() {
        // Create standard GameState led by SOUTH with SPADES as trump
        gameState = new GameState(PlayerPosition.SOUTH, Suit.SPADES);

        // Manually deal a predictable 52-card distribution for testing
        Deck deck = new Deck();
        PlayerPosition[] seats = PlayerPosition.values(); // SOUTH=0, WEST=1, NORTH=2, EAST=3
        int seatIdx = 0;
        deck.shuffle();

        // Standard test setup with known hands
        PlayerHand southHand = new PlayerHand(PlayerPosition.SOUTH);
        PlayerHand westHand  = new PlayerHand(PlayerPosition.WEST);
        PlayerHand northHand = new PlayerHand(PlayerPosition.NORTH);
        PlayerHand eastHand  = new PlayerHand(PlayerPosition.EAST);

        // Deal 13 cards to each seat
        dealDeterministicCards(southHand, westHand, northHand, eastHand);

        gameState.dealHand(PlayerPosition.SOUTH, southHand.getHand());
        gameState.dealHand(PlayerPosition.WEST, westHand.getHand());
        gameState.dealHand(PlayerPosition.NORTH, northHand.getHand());
        gameState.dealHand(PlayerPosition.EAST, eastHand.getHand());

        playingGateway = new PlayingGateway(gameState);
    }

    private void dealDeterministicCards(PlayerHand s, PlayerHand w, PlayerHand n, PlayerHand e) {
        // Give SOUTH all Spades (S2-SA)
        // Give WEST all Hearts (H2-HA)
        // Give NORTH all Diamonds (D2-DA)
        // Give EAST all Clubs (C2-CA)
        for (Rank r : Rank.values()) {
            s.addCard(new Card(Suit.SPADES, r));
            w.addCard(new Card(Suit.HEARTS, r));
            n.addCard(new Card(Suit.DIAMONDS, r));
            e.addCard(new Card(Suit.CLUBS, r));
        }
    }

    @Test
    @DisplayName("Test seat index translation and turn order tracking")
    void testCurrentTurnSeatIndex() {
        // SOUTH (index 0) leads first according to setup
        assertEquals(0, playingGateway.getCurrentTurnSeatIndex(), "Declarer SOUTH should lead initial turn.");
    }

    @Test
    @DisplayName("Test Card Code translation and legal card play execution")
    void testPlayCardSuccessAndTurnAdvance() {
        // SOUTH plays "S2" (2 of Spades)
        boolean accepted = playingGateway.playCard(0, "S2");

        assertTrue(accepted, "Playing a valid held card on current turn should return true.");

        // Turn should advance clockwise to WEST (seat index 1)
        assertEquals(1, playingGateway.getCurrentTurnSeatIndex(), "Turn should advance to WEST (index 1).");

        // Current trick should contain "S2"
        List<String> trickCards = playingGateway.getCurrentTrickCards();
        assertEquals(1, trickCards.size());
        assertEquals("S2", trickCards.get(0));
    }

    @Test
    @DisplayName("Test illegal play rejection when playing out of turn")
    void testPlayCardOutOfTurnRejected() {
        // WEST (index 1) tries to play before SOUTH (index 0)
        boolean accepted = playingGateway.playCard(1, "H2");

        assertFalse(accepted, "Playing out of turn must be rejected.");
        assertEquals(0, playingGateway.getCurrentTurnSeatIndex(), "Turn index must remain unchanged on rejected play.");
    }

    @Test
    @DisplayName("Test remaining hand representation after playing cards")
    void testGetRemainingHandForSeat() {
        List<String> southHand = playingGateway.getRemainingHandForSeat(0);
        assertEquals(13, southHand.size());
        assertTrue(southHand.contains("S10"), "Hand code string should follow <suit><rank> e.g. S10");

        // Play S10
        playingGateway.playCard(0, "S10");

        List<String> updatedHand = playingGateway.getRemainingHandForSeat(0);
        assertEquals(12, updatedHand.size());
        assertFalse(updatedHand.contains("S10"), "Played card must be removed from remaining hand view.");
    }

    @Test
    @DisplayName("Test completed tricks counter and hand completion state")
    void testTrickCompletionAndHandComplete() {
        assertFalse(playingGateway.isHandComplete());
        assertEquals(0, playingGateway.getCompletedTricksCount());

        // Play 1 full trick: S2 (SOUTH), H2 (WEST), D2 (NORTH), C2 (EAST)
        assertTrue(playingGateway.playCard(0, "S2")); // Led suit Spades
        assertTrue(playingGateway.playCard(1, "H2")); // Void in Spades -> plays Hearts
        assertTrue(playingGateway.playCard(2, "D2")); // Void in Spades -> plays Diamonds
        assertTrue(playingGateway.playCard(3, "C2")); // Void in Spades -> plays Clubs

        // Trick completed
        assertEquals(1, playingGateway.getCompletedTricksCount());
        assertFalse(playingGateway.isHandComplete());
    }

    @Test
    @DisplayName("Test invalid card code string throws IllegalArgumentException")
    void testInvalidCardCodeFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            playingGateway.playCard(0, "X10"); // Unknown suit letter
        });
    }
}