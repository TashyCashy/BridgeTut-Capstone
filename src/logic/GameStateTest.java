package logic;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void openingLeaderIsCorrectlyDerivedFromDeclarer() {
        GameState game = new GameState(PlayerPosition.SOUTH, Suit.SPADES);

        assertEquals(PlayerPosition.WEST, game.getCurrentPlayerTurn(),
                "The player to the declarer's left should lead first");
    }

    @Test
    void dealHandGivesPlayerTheirCards() {
        GameState game = new GameState(PlayerPosition.SOUTH, Suit.SPADES);
        List<Card> cards = List.of(
                new Card(Suit.HEARTS, Rank.TWO),
                new Card(Suit.CLUBS, Rank.KING),
                new Card(Suit.SPADES, Rank.ACE)
        );

        game.dealHand(PlayerPosition.SOUTH, new ArrayList<>(cards));

        assertEquals(3, game.getHand(PlayerPosition.SOUTH).getHand().size(),
                "The player's hand should contain every card they were dealt");
    }

    @Test
    void playCardRejectsAMoveOutOfTurn() {
        GameState game = new GameState(PlayerPosition.SOUTH, Suit.SPADES);
        Card card = new Card(Suit.HEARTS, Rank.TWO);
        game.dealHand(PlayerPosition.NORTH, new ArrayList<>(List.of(card)));

        // opening leader is WEST, so NORTH trying to play now is out of turn
        boolean result = game.playCard(PlayerPosition.NORTH, card);

        assertFalse(result, "A player should not be able to play when it isn't their turn");
        assertEquals(1, game.getHand(PlayerPosition.NORTH).getHand().size(),
                "An out-of-turn play should not remove the card from the player's hand");
    }

    @Test
    void playCardRejectsBreakingTheFollowSuitRule() {
        GameState game = new GameState(PlayerPosition.SOUTH, Suit.SPADES);

        // WEST leads first (opening leader)
        game.dealHand(PlayerPosition.WEST, new ArrayList<>(List.of(new Card(Suit.HEARTS, Rank.ACE))));
        game.playCard(PlayerPosition.WEST, new Card(Suit.HEARTS, Rank.ACE)); // hearts led

        // NORTH holds a heart AND a spade, but tries to play the spade instead
        Card heldHeart = new Card(Suit.HEARTS, Rank.SEVEN);
        Card offSuit = new Card(Suit.SPADES, Rank.TWO);
        game.dealHand(PlayerPosition.NORTH, new ArrayList<>(List.of(heldHeart, offSuit)));

        boolean result = game.playCard(PlayerPosition.NORTH, offSuit);

        assertFalse(result, "Playing off-suit while holding the led suit should be rejected");
        assertEquals(2, game.getHand(PlayerPosition.NORTH).getHand().size(),
                "A rejected play should not remove the card from the player's hand");
    }

    @Test
    void playCardAcceptsALegalMoveAndAdvancesTheTurn() {
        GameState game = new GameState(PlayerPosition.SOUTH, Suit.SPADES);
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        game.dealHand(PlayerPosition.WEST, new ArrayList<>(List.of(card)));

        boolean result = game.playCard(PlayerPosition.WEST, card);

        assertTrue(result, "A legal opening lead should be accepted");
        assertEquals(0, game.getHand(PlayerPosition.WEST).getHand().size(),
                "The played card should be removed from the player's hand");
        assertEquals(PlayerPosition.NORTH, game.getCurrentPlayerTurn(),
                "The turn should advance to the next player clockwise");
    }

    @Test
    void completingATrickArchivesItAndWinnerLeadsNext() {
        GameState game = new GameState(PlayerPosition.SOUTH, Suit.SPADES); // trump is SPADES

        // deliberately minimal, deterministic hands - one relevant card each
        game.dealHand(PlayerPosition.WEST, new ArrayList<>(List.of(new Card(Suit.HEARTS, Rank.ACE))));
        game.dealHand(PlayerPosition.NORTH, new ArrayList<>(List.of(new Card(Suit.SPADES, Rank.TWO)))); // trump, void in hearts
        game.dealHand(PlayerPosition.EAST, new ArrayList<>(List.of(new Card(Suit.HEARTS, Rank.KING))));
        game.dealHand(PlayerPosition.SOUTH, new ArrayList<>(List.of(new Card(Suit.HEARTS, Rank.QUEEN))));

        game.playCard(PlayerPosition.WEST, new Card(Suit.HEARTS, Rank.ACE));   // led
        game.playCard(PlayerPosition.NORTH, new Card(Suit.SPADES, Rank.TWO));  // only trump played - should win
        game.playCard(PlayerPosition.EAST, new Card(Suit.HEARTS, Rank.KING));
        game.playCard(PlayerPosition.SOUTH, new Card(Suit.HEARTS, Rank.QUEEN));

        assertEquals(1, game.getCompletedTricks().size(),
                "A trick with all 4 cards played should be archived as completed");
        assertEquals(PlayerPosition.NORTH, game.getCurrentPlayerTurn(),
                "The trick winner (the only player who played trump) should lead the next trick");
    }

    @Test
    void fullHandOfThirteenTricksCompletesCorrectly() {
        // build and shuffle a full 52-card deck
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);

        GameState game = new GameState(PlayerPosition.SOUTH, Suit.SPADES);
        PlayerPosition[] seats = { PlayerPosition.SOUTH, PlayerPosition.WEST, PlayerPosition.NORTH, PlayerPosition.EAST };
        int index = 0;
        for (PlayerPosition seat : seats) {
            game.dealHand(seat, new ArrayList<>(deck.subList(index, index + 13)));
            index += 13;
        }

        while (!game.isHandComplete()) {
            PlayerPosition current = game.getCurrentPlayerTurn();
            Card legalCard = pickLegalCard(game, current);
            boolean accepted = game.playCard(current, legalCard);
            assertTrue(accepted, "A card chosen specifically because it's legal should always be accepted");
        }

        assertEquals(13, game.getCompletedTricks().size(), "A full hand should consist of exactly 13 tricks");
        for (PlayerPosition seat : seats) {
            assertEquals(0, game.getHand(seat).getHand().size(),
                    "Every player's hand should be empty once the hand is complete");
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
