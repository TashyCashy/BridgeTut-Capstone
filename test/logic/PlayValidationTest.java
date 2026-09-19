package logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayValidationTest {

    private PlayerHand hand;
    private Trick trick;

    @BeforeEach
    void setUp() {
        hand = new PlayerHand(PlayerPosition.SOUTH);
        trick = new Trick(PlayerPosition.WEST);
    }

    @Test
    @DisplayName("Opening lead allows any card in hand")
    void openingLeadIsAlwaysLegal() {
        Card card = new Card(Suit.SPADES, Rank.ACE);
        hand.addCard(card);
        assertTrue(PlayValidation.isLegitPlay(hand, card, trick));
    }

    @Test
    @DisplayName("Must follow suit when holding cards of the led suit")
    void mustFollowSuitWhenHoldingIt() {
        Card ledCard = new Card(Suit.HEARTS, Rank.TEN);
        trick.recordPlay(PlayerPosition.WEST, ledCard);

        Card followSuitCard = new Card(Suit.HEARTS, Rank.TWO);
        Card renegeCard = new Card(Suit.CLUBS, Rank.ACE);

        hand.addCard(followSuitCard);
        hand.addCard(renegeCard);

        assertTrue(PlayValidation.isLegitPlay(hand, followSuitCard, trick));
        assertFalse(PlayValidation.isLegitPlay(hand, renegeCard, trick));
    }

    @Test
    @DisplayName("Any card is legal when void in the led suit")
    void anyCardIsLegalWhenVoidInLedSuit() {
        Card ledCard = new Card(Suit.HEARTS, Rank.TEN);
        trick.recordPlay(PlayerPosition.WEST, ledCard);

        Card offSuitCard = new Card(Suit.CLUBS, Rank.ACE);
        hand.addCard(offSuitCard);

        assertTrue(PlayValidation.isLegitPlay(hand, offSuitCard, trick));
    }

    @Test
    @DisplayName("Highest card of led suit wins when no trumps played")
    void highestLedSuitWinsNoTrump() {
        trick.recordPlay(PlayerPosition.SOUTH, new Card(Suit.DIAMONDS, Rank.FIVE));
        trick.recordPlay(PlayerPosition.WEST, new Card(Suit.DIAMONDS, Rank.KING));
        trick.recordPlay(PlayerPosition.NORTH, new Card(Suit.DIAMONDS, Rank.ACE));
        trick.recordPlay(PlayerPosition.EAST, new Card(Suit.DIAMONDS, Rank.JACK));

        PlayerPosition winner = PlayValidation.pickWinner(trick, null);
        assertEquals(PlayerPosition.NORTH, winner);
    }

    @Test
    @DisplayName("Trump card beats a higher rank non-trump card")
    void trumpBeatsHigherNonTrumpCard() {
        Suit trumpSuit = Suit.SPADES;

        trick.recordPlay(PlayerPosition.SOUTH, new Card(Suit.HEARTS, Rank.ACE)); // Led suit
        trick.recordPlay(PlayerPosition.WEST, new Card(Suit.HEARTS, Rank.KING));
        trick.recordPlay(PlayerPosition.NORTH, new Card(Suit.SPADES, Rank.TWO)); // Trump
        trick.recordPlay(PlayerPosition.EAST, new Card(Suit.HEARTS, Rank.QUEEN));

        PlayerPosition winner = PlayValidation.pickWinner(trick, trumpSuit);
        assertEquals(PlayerPosition.NORTH, winner);
    }

    @Test
    @DisplayName("Higher trump beats lower trump")
    void higherTrumpBeatsLowerTrump() {
        Suit trumpSuit = Suit.CLUBS;

        trick.recordPlay(PlayerPosition.SOUTH, new Card(Suit.DIAMONDS, Rank.ACE)); // Led suit
        trick.recordPlay(PlayerPosition.WEST, new Card(Suit.CLUBS, Rank.FIVE));    // Trump
        trick.recordPlay(PlayerPosition.NORTH, new Card(Suit.CLUBS, Rank.JACK));   // Higher Trump
        trick.recordPlay(PlayerPosition.EAST, new Card(Suit.DIAMONDS, Rank.KING));

        PlayerPosition winner = PlayValidation.pickWinner(trick, trumpSuit);
        assertEquals(PlayerPosition.NORTH, winner);
    }

    @Test
    @DisplayName("Off-suit discard never wins even with high rank")
    void offSuitDiscardNeverWins() {
        trick.recordPlay(PlayerPosition.SOUTH, new Card(Suit.CLUBS, Rank.FOUR));
        trick.recordPlay(PlayerPosition.WEST, new Card(Suit.SPADES, Rank.ACE)); // Off-suit discard
        trick.recordPlay(PlayerPosition.NORTH, new Card(Suit.CLUBS, Rank.NINE));
        trick.recordPlay(PlayerPosition.EAST, new Card(Suit.CLUBS, Rank.TWO));

        PlayerPosition winner = PlayValidation.pickWinner(trick, null);
        assertEquals(PlayerPosition.NORTH, winner);
    }
}