package tests.Tutorial;

import BiddingSystem.Tutorial.TutorialGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TutorialGatewayTest {

    private TutorialGateway gateway;
    private Path tempLessonFile;

    @BeforeEach
    void setUp() throws IOException {
        gateway = new TutorialGateway();
        
        String lessonContent = 
            "Cards held: spades;hearts;diamonds;clubs\n" +
            "N: 7654;A85;9;AK983\n" +
            "S: AQ2;J7;KT85;QJ76\n" +
            "W: KT9;K9632;72;T54\n" +
            "E: J83;QT4;AQJ643;2\n" +
            "PLAY 3NT\n" +
            "7D,9D,AD,5D\n" +
            "Concede\n" +
            "NOTE: Continue Diamonds.";

        tempLessonFile = Files.createTempFile("test_lesson", ".txt");
        Files.writeString(tempLessonFile, lessonContent);
    }

    @Test
    @DisplayName("Load lesson from text file via Gateway")
    void testLoadLessonTextSuccess() {
        boolean loaded = gateway.loadLessonText(tempLessonFile.toString());
        assertTrue(loaded);
        assertFalse(gateway.isTutorialComplete());
        assertEquals(0, gateway.getMistakeCount());
    }

    @Test
    @DisplayName("Handle nonexistent file gracefully without crashing")
    void testLoadNonexistentFile() {
        boolean loaded = gateway.loadLessonText("non_existent_file.txt");
        assertFalse(loaded);
        assertEquals(-1, gateway.getCurrentTurnSeatIndex());
    }

    @Test
    @DisplayName("Expose Concede action to Python Py4J caller")
    void testGatewayConcedeTricks() {
        gateway.loadLessonText(tempLessonFile.toString());
        boolean result = gateway.concedeTricks();
        assertTrue(result);
        assertTrue(gateway.isTutorialComplete());
        assertEquals("CONCEDE", gateway.getFinalOutcome());
        assertEquals("Continue Diamonds.", gateway.getLessonNote());
    }
}