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

public class TutorialGatewayTest {

    private TutorialGateway gateway;

    @BeforeEach
    void setUp() {
        Lesson lesson = new Lesson();
        lesson.declarer = PlayerPosition.SOUTH;
        lesson.trumpSuit = Suit.SPADES;
        lesson.outcome = LessonOutcome.CONCEDE;
        lesson.note = "Concede lesson test.";

        // Script 1 trick: C4, C6, CA, C2
        List<Card> trick1 = List.of(
                new Card(Suit.CLUBS, Rank.FOUR),
                new Card(Suit.CLUBS, Rank.SIX),
                new Card(Suit.CLUBS, Rank.ACE),
                new Card(Suit.CLUBS, Rank.TWO)
        );
        lesson.tricks.add(trick1);

        TutorialEngine engine = new TutorialEngine(lesson);
        gateway = new TutorialGateway(engine);
    }

    @Test
    @DisplayName("Uninitialized gateway handles queries gracefully without throwing NPE")
    void testUninitializedGatewaySafety() {
        TutorialGateway emptyGateway = new TutorialGateway();
        assertEquals(-1, emptyGateway.getCurrentTurnSeatIndex());
        assertEquals(0, emptyGateway.getMistakeCount());
        assertFalse(emptyGateway.isTutorialComplete());
        assertEquals("", emptyGateway.getLessonNote());
        assertEquals("IN_PROGRESS", emptyGateway.getFinalOutcome());
    }

    @Test
    @DisplayName("Handle nonexistent file gracefully without crashing")
    void testLoadNonexistentFile() {
        assertFalse(gateway.loadLessonText("non_existent_file.txt"));
        assertEquals(-1, gateway.getCurrentTurnSeatIndex());
    }

    @Test
    @DisplayName("Expose Concede action to Python Py4J caller")
    void testGatewayConcedeTricks() {
        // Play through all scripted cards first (check null and empty)
        while (gateway.getExpectedCardCode() != null && !gateway.getExpectedCardCode().isEmpty()) {
            int seat = gateway.getCurrentTurnSeatIndex();
            String card = gateway.getExpectedCardCode();
            gateway.playCard(seat, card);
        }

        // Concede succeeds after scripted tricks complete
        assertTrue(gateway.concedeTricks());
        assertTrue(gateway.isTutorialComplete());
        assertEquals("CONCEDE", gateway.getFinalOutcome());
    }
}