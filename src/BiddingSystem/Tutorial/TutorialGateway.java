package BiddingSystem.Tutorial;

import LessonTutorial.Lesson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TutorialGateway {
    private TutorialEngine engine;

    // default constructor for py4j entry point creation
    public TutorialGateway () {}

    public TutorialGateway(TutorialEngine engine) {
        this.engine = engine;
    }

    public int getCurrentTurnSeatIndex() {
        if (engine != null)
            return engine.getCurrentTurnSeatIndex();
        else 
            return -1;
    }

    public int getMistakeCount() {
        return engine.getMistakeCount();
    }

    public boolean isTutorialComplete() {
        return engine.isTutorialComplete();
    }

    public boolean playCard(int seatIdx, String cardCode) {
        return engine.playCard(seatIdx, cardCode);
    }

    public boolean claimTricks() {
        return engine.claim();
    }

    public boolean concedeTricks() {
        return engine.concede();
    }

    public String getFinalOutcome() {
        if (engine.getFinalOutcome() != null) 
            return engine.getFinalOutcome().name();
        else
            return "IN_PROGRESS";
    }

    public String getLessonNote() {
        return engine.getLessonNote();
    }

    public boolean loadLessonText(String filePath) {
        try {
            String rawText = Files.readString(Path.of(filePath));
            Lesson lesson = LessonParser.parseLessonText(rawText);
            engine = new TutorialEngine(lesson);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load lesson file: " + e.getMessage());
            engine = null; // ensure engine is null on failure
            return false;
        }
    }
}