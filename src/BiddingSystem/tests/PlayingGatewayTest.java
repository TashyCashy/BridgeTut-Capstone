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
    private PlayerPosition declarer;
    private int expectedLeadIdx;

    @BeforeEach
    void setUp() {
        // Choose declarer seat dynamically (e.g. SOUTH)
        declarer = PlayerPosition.SOUTH;

        // Compute opening leader index (left of declarer -> SOUTH(0) + 1 = WEST(1))
        expectedLeadIdx = (declarer.ordinal() + 1) % 4;

        // GameState with chosen declarer and trump suit
        gameState = new GameState(declarer, Suit.SPADES);

        // Standard test setup with known hands:
        // SOUTH = Spades, WEST = Hearts, NORTH = Diamonds, EAST = Clubs
        PlayerHand southHand = new PlayerHand(PlayerPosition.SOUTH);
        PlayerHand westHand  = new PlayerHand(PlayerPosition.WEST);
        PlayerHand northHand = new PlayerHand(PlayerPosition.NORTH);
        PlayerHand eastHand  = new PlayerHand(PlayerPosition.EAST);

        dealDeterministicCards(southHand, westHand, northHand, eastHand);

        gameState.dealHand(PlayerPosition.SOUTH, southHand.getHand());
        gameState.dealHand(PlayerPosition.WEST, westHand.getHand());
        gameState.dealHand(PlayerPosition.NORTH, northHand.getHand());
        gameState.dealHand(PlayerPosition.EAST, eastHand.getHand());

        playingGateway = new PlayingGateway(gameState);
    }

    private void dealDeterministicCards(PlayerHand s, PlayerHand w, PlayerHand n, PlayerHand e) {
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
        // In Bridge, opening lead is left of declarer (WEST = index 1)
        assertEquals(expectedLeadIdx, playingGateway.getCurrentTurnSeatIndex(), 
            "Player to declarer's left should lead initial turn.");
    }

    @Test
    @DisplayName("Test Card Code translation and legal card play execution")
    void testPlayCardSuccessAndTurnAdvance() {
        // WEST (index 1) leads "H2" on turn 1
        boolean accepted = playingGateway.playCard(expectedLeadIdx, "H2");
        assertTrue(accepted, "Playing a valid held card on current turn should return true.");

        // Turn advances clockwise to NORTH (index 2)
        int nextTurn = (expectedLeadIdx + 1) % 4;
        assertEquals(nextTurn, playingGateway.getCurrentTurnSeatIndex(), "Turn should advance to NORTH (index 2).");

        List<String> trickCards = playingGateway.getCurrentTrickCards();
        assertEquals(1, trickCards.size());
        assertEquals("H2", trickCards.get(0));
    }

    @Test
    @DisplayName("Test illegal play rejection when playing out of turn")
    void testPlayCardOutOfTurnRejected() {
        // SOUTH (index 0) tries to play before WEST (index 1)
        int wrongTurnSeat = declarer.ordinal(); // 0
        boolean accepted = playingGateway.playCard(wrongTurnSeat, "S2");

        assertFalse(accepted, "Playing out of turn must be rejected.");
        assertEquals(expectedLeadIdx, playingGateway.getCurrentTurnSeatIndex(), 
            "Turn index must remain unchanged on rejected play.");
    }

    @Test
    @DisplayName("Test remaining hand representation after playing cards")
    void testGetRemainingHandForSeat() {
        // WEST (index 1) has 13 Hearts
        List<String> westHand = playingGateway.getRemainingHandForSeat(expectedLeadIdx);
        assertEquals(13, westHand.size());
        assertTrue(westHand.contains("H10"));

        // WEST plays H10 on their turn
        playingGateway.playCard(expectedLeadIdx, "H10");

        List<String> updatedHand = playingGateway.getRemainingHandForSeat(expectedLeadIdx);
        assertEquals(12, updatedHand.size());
        assertFalse(updatedHand.contains("H10"), "Played card must be removed from remaining hand.");
    }

    @Test
    @DisplayName("Test completed tricks counter and hand completion state")
    void testTrickCompletionAndHandComplete() {
        assertFalse(playingGateway.isHandComplete());
        assertEquals(0, playingGateway.getCompletedTricksCount());

        // Play 1 full trick starting with leader WEST (1): WEST (H2), NORTH (D2), EAST (C2), SOUTH (S2)
        assertTrue(playingGateway.playCard(1, "H2")); 
        assertTrue(playingGateway.playCard(2, "D2")); 
        assertTrue(playingGateway.playCard(3, "C2")); 
        assertTrue(playingGateway.playCard(0, "S2")); 

        assertEquals(1, playingGateway.getCompletedTricksCount());
        assertFalse(playingGateway.isHandComplete());
    }

    @Test
    @DisplayName("Test invalid card code string throws IllegalArgumentException")
    void testInvalidCardCodeFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            playingGateway.playCard(expectedLeadIdx, "X10");
        });
    }
}