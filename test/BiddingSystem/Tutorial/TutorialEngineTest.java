package BiddingSystem.Tutorial;

import LessonTutorial.Lesson;
import LessonTutorial.LessonOutcome;
import BiddingSystem.BiddingData.Actions.*;
import logic.Card;
import logic.PlayerPosition;
import logic.Rank;
import logic.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TutorialEngineTest {

    private TutorialEngine engine;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        lesson = new Lesson();
        lesson.declarer = PlayerPosition.SOUTH;
        lesson.trumpSuit = Suit.SPADES;
        lesson.outcome = LessonOutcome.CLAIM;
        lesson.note = "Always plan your tricks.";

        // Script 1 trick: C4, C6, CA, C2 (led by WEST)
        List<Card> trick1 = List.of(
                new Card(Suit.CLUBS, Rank.FOUR),
                new Card(Suit.CLUBS, Rank.SIX),
                new Card(Suit.CLUBS, Rank.ACE),
                new Card(Suit.CLUBS, Rank.TWO)
        );
        lesson.tricks.add(trick1);

        engine = new TutorialEngine(lesson);
    }

    @Test
    @DisplayName("getExpectedCardCode returns exact card expected by script")
    void testGetExpectedCardCode() {
        // First card expected from WEST is 4 of Clubs -> "C4"
        assertEquals("C4", engine.getExpectedCardCode());
    }

    @Test
    @DisplayName("Reject illegal card play and increment mistake count")
    void testRejectIllegalCardPlay() {
        int initialMistakes = engine.getMistakeCount();

        // WEST plays "CA" instead of "C4"
        assertFalse(engine.playCard(1, "CA"));
        assertEquals(initialMistakes + 1, engine.getMistakeCount());
    }

    @Test
    @DisplayName("Accept valid expected card play and advance turn")
    void testAcceptValidCardPlay() {
        // WEST plays "C4"
        assertTrue(engine.playCard(1, "C4"));

        // Next turn is NORTH (seat 2), expecting "C6"
        assertEquals(2, engine.getCurrentTurnSeatIndex());
        assertEquals("C6", engine.getExpectedCardCode());
    }

    @Test
    @DisplayName("Claiming before tricks finish fails and increments mistake count")
    void testClaimPrematurelyFails() {
        assertFalse(engine.claim());
        assertEquals(1, engine.getMistakeCount());
        assertFalse(engine.isTutorialComplete());
    }

    @Test
    @DisplayName("Claiming after all scripted tricks pass succeeds")
    void testClaimAfterTricksSucceeds() {
        // Play through trick 1 completely
        engine.playCard(1, "C4"); // West
        engine.playCard(2, "C6"); // North
        engine.playCard(3, "CA"); // East
        engine.playCard(0, "C2"); // South

        // All scripted tricks done
        assertTrue(engine.isAwaitingClaimConcede());

        // Claim now succeeds
        assertTrue(engine.claim());
        assertTrue(engine.isTutorialComplete());
        assertEquals(LessonOutcome.CLAIM, engine.getFinalOutcome());
    }

    @Test
    @DisplayName("Handle Claim auto-completion successfully when expected by script")
    void testHandleClaimSuccess() {
        // Play through all scripted tricks first
        while (engine.getExpectedCardCode() != null) {
            int seat = engine.getCurrentTurnSeatIndex();
            String card = engine.getExpectedCardCode();
            engine.playCard(seat, card);
        }

        // Now claim succeeds as all scripted cards are played
        assertTrue(engine.claim());
        assertTrue(engine.isTutorialComplete());
        assertEquals(LessonOutcome.CLAIM, engine.getFinalOutcome());
    }

    @Test
    @DisplayName("Reject Concede auto-completion when script expects Claim")
    void testRejectConcedeWhenClaimExpected() {
        // Play through all scripted tricks first
        while (engine.getExpectedCardCode() != null) {
            int seat = engine.getCurrentTurnSeatIndex();
            String card = engine.getExpectedCardCode();
            engine.playCard(seat, card);
        }

        // Attempting concede when lesson.outcome == CLAIM fails
        assertFalse(engine.concede());
        assertFalse(engine.isTutorialComplete());
    }
}