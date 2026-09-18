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

    public int getLeaderSeatIndex() {
        if (engine != null)
            return engine.getLeaderSeatIndex();
        return -1;
    }

    public int getMistakeCount() {
        if (engine != null)
            return engine.getMistakeCount();
        return 0;
    }

    public boolean isTutorialComplete() {
        return (engine != null) && engine.isTutorialComplete();
    }

    public boolean playCard(int seatIdx, String cardCode) {
        return (engine != null) && engine.playCard(seatIdx, cardCode);
    }

    public boolean claimTricks() {
        return (engine != null) && engine.claim();
    }

    public boolean concedeTricks() {
        return (engine != null) && engine.concede();
    }

    public String getFinalOutcome() {
        if (engine != null && engine.getFinalOutcome() != null) 
            return engine.getFinalOutcome().name();
        else
            return "IN_PROGRESS";
    }

    public String getLessonNote() {
        if (engine != null) 
            return engine.getLessonNote();
        return "";
    }

    public String getExpectedCardCode() {
        if (engine != null)
            return engine.getExpectedCardCode();
        return "";
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

    // expose hand card codes for a seat to python through py4j
    public java.util.List<String> getHandForSeat(int seatIdx) {
        if (engine != null)
            return engine.getHandForSeat(seatIdx);
        return new java.util.ArrayList<>();
    }
}