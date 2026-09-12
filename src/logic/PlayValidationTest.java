package logic;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlayValidationTest {

    @Test
    void openingLeadIsAlwaysLegal() {
        PlayerHand hand = new PlayerHand(PlayerPosition.SOUTH);
        hand.addCard(new Card(Suit.HEARTS, Rank.SEVEN));
        Trick trick = new Trick(PlayerPosition.SOUTH); // no card played yet, ledSuit is null

        boolean result = PlayValidation.isLegitPlay(hand, new Card(Suit.HEARTS, Rank.SEVEN), trick);

        assertTrue(result, "The very first card of a trick should always be legal");
    }

    @Test
    void mustFollowSuitWhenHoldingIt() {
        PlayerHand hand = new PlayerHand(PlayerPosition.SOUTH);
        hand.addCard(new Card(Suit.HEARTS, Rank.SEVEN));
        hand.addCard(new Card(Suit.SPADES, Rank.TWO));

        Trick trick = new Trick(PlayerPosition.WEST);
        trick.recordPlay(PlayerPosition.WEST, new Card(Suit.HEARTS, Rank.KING)); // hearts led

        boolean followsSuit = PlayValidation.isLegitPlay(hand, new Card(Suit.HEARTS, Rank.SEVEN), trick);
        boolean breaksRule = PlayValidation.isLegitPlay(hand, new Card(Suit.SPADES, Rank.TWO), trick);

        assertTrue(followsSuit, "Playing the led suit when you hold it should be legal");
        assertFalse(breaksRule, "Playing off-suit while holding the led suit should be illegal");
    }

    @Test
    void anyCardIsLegalWhenVoidInLedSuit() {
        PlayerHand hand = new PlayerHand(PlayerPosition.SOUTH);
        hand.addCard(new Card(Suit.SPADES, Rank.TWO)); // no hearts at all

        Trick trick = new Trick(PlayerPosition.WEST);
        trick.recordPlay(PlayerPosition.WEST, new Card(Suit.HEARTS, Rank.KING)); // hearts led

        boolean result = PlayValidation.isLegitPlay(hand, new Card(Suit.SPADES, Rank.TWO), trick);

        assertTrue(result, "Any card should be legal when the player is void in the led suit");
    }

    @Test
    void trumpBeatsHigherNonTrumpCard() {
        Trick trick = new Trick(PlayerPosition.SOUTH);
        trick.recordPlay(PlayerPosition.SOUTH, new Card(Suit.HEARTS, Rank.ACE)); // led, high card
        trick.recordPlay(PlayerPosition.WEST, new Card(Suit.SPADES, Rank.TWO));  // trump, low rank
        trick.recordPlay(PlayerPosition.NORTH, new Card(Suit.HEARTS, Rank.KING));
        trick.recordPlay(PlayerPosition.EAST, new Card(Suit.HEARTS, Rank.QUEEN));

        PlayerPosition winner = PlayValidation.pickWinner(trick, Suit.SPADES);

        assertEquals(PlayerPosition.WEST, winner, "A low trump should beat a high card of the suit led");
    }

    @Test
    void higherTrumpBeatsLowerTrump() {
        Trick trick = new Trick(PlayerPosition.SOUTH);
        trick.recordPlay(PlayerPosition.SOUTH, new Card(Suit.SPADES, Rank.THREE));
        trick.recordPlay(PlayerPosition.WEST, new Card(Suit.SPADES, Rank.JACK));
        trick.recordPlay(PlayerPosition.NORTH, new Card(Suit.SPADES, Rank.TWO));
        trick.recordPlay(PlayerPosition.EAST, new Card(Suit.SPADES, Rank.SEVEN));

        PlayerPosition winner = PlayValidation.pickWinner(trick, Suit.SPADES);

        assertEquals(PlayerPosition.WEST, winner, "The highest trump should win when all 4 cards are trump");
    }

    @Test
    void noTrumpGameHighestOfLedSuitWins() {
        Trick trick = new Trick(PlayerPosition.SOUTH);
        trick.recordPlay(PlayerPosition.SOUTH, new Card(Suit.HEARTS, Rank.TWO));
        trick.recordPlay(PlayerPosition.WEST, new Card(Suit.HEARTS, Rank.ACE));
        trick.recordPlay(PlayerPosition.NORTH, new Card(Suit.SPADES, Rank.ACE)); // off-suit, discard
        trick.recordPlay(PlayerPosition.EAST, new Card(Suit.HEARTS, Rank.KING));

        PlayerPosition winner = PlayValidation.pickWinner(trick, null); // null = No Trump

        assertEquals(PlayerPosition.WEST, winner, "In NT, the highest card of the suit led should win");
    }

    @Test
    void offSuitDiscardNeverWinsEvenWithHighRank() {
        Trick trick = new Trick(PlayerPosition.SOUTH);
        trick.recordPlay(PlayerPosition.SOUTH, new Card(Suit.HEARTS, Rank.TWO));
        trick.recordPlay(PlayerPosition.WEST, new Card(Suit.SPADES, Rank.ACE)); // off-suit, non-trump, high rank
        trick.recordPlay(PlayerPosition.NORTH, new Card(Suit.HEARTS, Rank.THREE));
        trick.recordPlay(PlayerPosition.EAST, new Card(Suit.HEARTS, Rank.FOUR));

        PlayerPosition winner = PlayValidation.pickWinner(trick, Suit.CLUBS); // trump is clubs, nobody played clubs

        assertEquals(PlayerPosition.EAST, winner,
                "An off-suit, non-trump card should never win, regardless of its rank");
    }
}
