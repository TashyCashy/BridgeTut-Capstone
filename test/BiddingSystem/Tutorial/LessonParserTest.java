package BiddingSystem.Tutorial;

import LessonTutorial.Lesson;
import LessonTutorial.LessonOutcome;
import logic.PlayerPosition;
import logic.Suit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LessonParserTest {

    private final String sampleLessonText = 
            "Cards held: spades;hearts;diamonds;clubs\n" +
            "N: KQ52;JT652;AQ42;\n" +
            "S: AJT984;K3;K;T432\n" +
            "W: 63;AQ4;T953;Q976\n" +
            "E: 7;987;J876;AKJ85\n" +
            "PLAY 4S\n" +
            "6C,2S,5C,2C\n" +
            "Claim\n" +
            "NOTE: Test lesson note explanation.";

    @Test
    @DisplayName("parseLessonText correctly parses hands, contract, tricks, outcome, and notes")
    void parsesSingleLessonText() {
        Lesson lesson = LessonParser.parseLessonText(sampleLessonText);

        assertNotNull(lesson);
        assertEquals("4S", lesson.contractString);
        assertEquals(Suit.SPADES, lesson.trumpSuit);
        assertEquals(PlayerPosition.SOUTH, lesson.declarer);
        assertEquals(LessonOutcome.CLAIM, lesson.outcome);
        assertEquals("Test lesson note explanation.", lesson.note);

        // Check South's hand card count
        assertEquals(13, lesson.getHandForSeat(PlayerPosition.SOUTH).size());

        // Check trick count
        assertEquals(1, lesson.tricks.size());
        assertEquals(4, lesson.tricks.get(0).size());
    }
}