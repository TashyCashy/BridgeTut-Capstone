package logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    private GameState game;

    @BeforeEach
    void setUp() {
        game = new GameState(PlayerPosition.SOUTH, Suit.SPADES);
    }

    @Test
    @DisplayName("Opening leader is correctly derived as declarer.next()")
    void openingLeaderIsCorrect() {
        // Declarer is SOUTH, so opening leader must be WEST
        assertEquals(PlayerPosition.WEST, game.getCurrentPlayerTurn());
    }

    @Test
    @DisplayName("Dealing hands correctly populates player cards")
    void dealHandPopulatesCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(new Card(Suit.SPADES, Rank.ACE));
        cards.add(new Card(Suit.HEARTS, Rank.KING));

        game.dealHand(PlayerPosition.WEST, cards);

        assertEquals(2, game.getHand(PlayerPosition.WEST).size());
    }

    @Test
    @DisplayName("Reject move when played out of turn")
    void rejectsMoveOutOfTurn() {
        List<Card> southCards = List.of(new Card(Suit.CLUBS, Rank.TEN));
        game.dealHand(PlayerPosition.SOUTH, southCards);

        // Turn is currently WEST, so SOUTH trying to play should be rejected
        assertFalse(game.playCard(PlayerPosition.SOUTH, southCards.get(0)));
    }

    @Test
    @DisplayName("Legal card play advances turn and updates trick state")
    void legalPlayAdvancesTurn() {
        List<Card> westCards = List.of(new Card(Suit.CLUBS, Rank.TEN));
        game.dealHand(PlayerPosition.WEST, westCards);

        assertTrue(game.playCard(PlayerPosition.WEST, westCards.get(0)));
        assertEquals(PlayerPosition.NORTH, game.getCurrentPlayerTurn());
    }
}