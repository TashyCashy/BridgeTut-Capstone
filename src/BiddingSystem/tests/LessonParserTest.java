package tests.Tutorial;

import LessonTutorial.Lesson;
import LessonTutorial.LessonOutcome;
import BiddingSystem.Tutorial.LessonParser;
import logic.PlayerPosition;
import logic.Suit;
import logic.Rank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LessonParserTest {

    @Test
    @DisplayName("Parse valid Mode 2 play lesson with missing suit and Claim outcome")
    void testParseMode2ClaimLesson() {
        String rawText = 
            "Cards held: spades;hearts;diamonds;clubs\n" +
            "N: KQ52;JT652;AQ42;\n" +
            "S: AJT984; K3;K;T432\n" +
            "W: 63;AQ4;T953;Q976\n" +
            "E: 7;987;J876;AKJ85\n" +
            "PLAY 4S\n" +
            "6C,2S,5C,2C\n" +
            "2D,6D,KD,3D\n" +
            "Claim\n" +
            "NOTE: Cater for 4-2 losers in Clubs.";

        Lesson lesson = LessonParser.parseLessonText(rawText);

        assertNotNull(lesson);
        assertEquals("4S", lesson.contractString);
        assertEquals(LessonOutcome.CLAIM, lesson.outcome);
        assertEquals(2, lesson.tricks.size());
        assertEquals("Cater for 4-2 losers in Clubs.", lesson.note);

        // North missing clubs check
        assertEquals(0, lesson.hands.get(PlayerPosition.NORTH).stream()
                .filter(c -> c.getSuit() == Suit.CLUBS).count());

        // North spade count check
        assertEquals(4, lesson.hands.get(PlayerPosition.NORTH).stream()
                .filter(c -> c.getSuit() == Suit.SPADES).count());
    }

    @Test
    @DisplayName("Parse Mode 2 lesson with Concede outcome")
    void testParseMode2ConcedeLesson() {
        String rawText = 
            "Cards held: spades;hearts;diamonds;clubs\n" +
            "N: QJ3;8543;9863;52\n" +
            "S: AKT74;76;5;AKQ43\n" +
            "W: 985;QT9;QJT42;96\n" +
            "E: 62;AKJ2;AK7;JT87\n" +
            "PLAY 3S\n" +
            "QD,3D,7D,5D\n" +
            "Concede\n" +
            "NOTE: Ruff high on first ruff.";

        Lesson lesson = LessonParser.parseLessonText(rawText);

        assertNotNull(lesson);
        assertEquals("3S", lesson.contractString);
        assertEquals(LessonOutcome.CONCEDE, lesson.outcome);
        assertEquals(1, lesson.tricks.size());
    }

    @Test
    @DisplayName("Throw exception when lesson file header is invalid")
    void testInvalidHeaderThrowsException() {
        String invalidText = "Invalid Header Line\nN: KQ52;JT652;AQ42;";
        assertThrows(IllegalArgumentException.class, () -> LessonParser.parseLessonText(invalidText));
    }
}