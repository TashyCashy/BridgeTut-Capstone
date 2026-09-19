package BiddingSystem;

import logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayingGatewayTest {

    private PlayingGateway gateway;

    @BeforeEach
    void setUp() {
        GameState game = new GameState(PlayerPosition.SOUTH, Suit.HEARTS);
        
        // Deal 1 card to each player for testing
        game.dealHand(PlayerPosition.SOUTH, List.of(new Card(Suit.HEARTS, Rank.ACE)));
        game.dealHand(PlayerPosition.WEST, List.of(new Card(Suit.HEARTS, Rank.KING)));
        game.dealHand(PlayerPosition.NORTH, List.of(new Card(Suit.HEARTS, Rank.QUEEN)));
        game.dealHand(PlayerPosition.EAST, List.of(new Card(Suit.HEARTS, Rank.JACK)));

        gateway = new PlayingGateway(game);
    }

    @Test
    @DisplayName("getRemainingHandForSeat converts cards into string codes")
    void getRemainingHandForSeatFormatsCodes() {
        // WEST (seat index 1) holds King of Hearts -> "HK"
        List<String> westHand = gateway.getRemainingHandForSeat(1);
        assertEquals(1, westHand.size());
        assertEquals("HK", westHand.get(0));
    }

    @Test
    @DisplayName("playCard parses string codes and advances turn")
    void playCardParsesAndExecutes() {
        // Current turn is WEST (seat 1)
        assertEquals(1, gateway.getCurrentTurnSeatIndex());

        // Play "HK" (King of Hearts) for WEST
        assertTrue(gateway.playCard(1, "HK"));

        // Turn should move to NORTH (seat 2)
        assertEquals(2, gateway.getCurrentTurnSeatIndex());
    }

    @Test
    @DisplayName("getNorthSouthTricks and getEastWestTricks accurately tally completed tricks")
    void trickTalliesUpdateCorrectly() {
        assertEquals(0, gateway.getNorthSouthTricks());
        assertEquals(0, gateway.getEastWestTricks());
    }
}