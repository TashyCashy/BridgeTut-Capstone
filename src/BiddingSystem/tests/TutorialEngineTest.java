package tests.Tutorial;

import LessonTutorial.Lesson;
import LessonTutorial.LessonOutcome;
import BiddingSystem.Tutorial.LessonParser;
import BiddingSystem.Tutorial.TutorialEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TutorialEngineTest {

    private Lesson lesson;
    private TutorialEngine engine;

    @BeforeEach
    void setUp() {
        String rawText = 
            "Cards held: spades;hearts;diamonds;clubs\n" +
            "N: KQ52;JT652;AQ42;\n" +
            "S: AJT984; K3;K;T432\n" +
            "W: 63;AQ4;T953;Q976\n" +
            "E: 7;987;J876;AKJ85\n" +
            "PLAY 4S\n" +
            "6C,2S,5C,2C\n" +
            "Claim\n" +
            "NOTE: Play safe in clubs.";

        lesson = LessonParser.parseLessonText(rawText);
        engine = new TutorialEngine(lesson);
    }

    @Test
    @DisplayName("Accept valid expected card play and advance turn")
    void testPlayValidCard() {
        int initialSeat = engine.getCurrentTurnSeatIndex();
        assertNotEquals(-1, initialSeat);

        boolean success = engine.playCard(initialSeat, "C6");
        assertTrue(success);
        assertEquals(0, engine.getMistakeCount());
    }

    @Test
    @DisplayName("Reject illegal card play and increment mistake count")
    void testPlayInvalidCardIncrementsMistakes() {
        int initialSeat = engine.getCurrentTurnSeatIndex();
        
        // Play wrong card code "SA" instead of expected "C6"
        boolean success = engine.playCard(initialSeat, "SA");
        assertFalse(success);
        assertEquals(1, engine.getMistakeCount());
    }

    @Test
    @DisplayName("Handle Claim auto-completion successfully when expected by script")
    void testHandleClaimSuccess() {
        assertFalse(engine.isTutorialComplete());
        boolean claimed = engine.claim();
        assertTrue(claimed);
        assertTrue(engine.isTutorialComplete());
        assertEquals(LessonOutcome.CLAIM, engine.getFinalOutcome());
    }

    @Test
    @DisplayName("Reject Concede auto-completion when script expects Claim")
    void testHandleConcedeFailsWhenClaimExpected() {
        boolean conceded = engine.concede();
        assertFalse(conceded);
        assertEquals(1, engine.getMistakeCount());
        assertFalse(engine.isTutorialComplete());
    }
}