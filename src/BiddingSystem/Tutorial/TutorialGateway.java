package BiddingSystem.Tutorial;

import BiddingSystem.BiddingData.Actions.*;
import LessonTutorial.Lesson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import logic.Strain;

/**
 * Py4J gateway exposing interactive tutorial management to Python[cite: 3, 24].
 * Translates lesson states, script validation, and bidding actions across the language boundary[cite: 24].
 */
public class TutorialGateway {
    private TutorialEngine engine;
    private List<Lesson> lessons = new ArrayList<>();
    private int currentLessonIndex = -1;

    /**
     * Default constructor required for Py4J entry point creation[cite: 24].
     */
    public TutorialGateway() {}

    /**
     * Constructs a TutorialGateway wrapping an active engine[cite: 24].
     *
     * @param engine Active {@link TutorialEngine}[cite: 24].
     */
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

    /**
     * Loads a single lesson from a text file path[cite: 24].
     *
     * @param filePath File path string[cite: 24].
     * @return {@code true} if successfully loaded; {@code false} on error[cite: 24].
     */
    public boolean loadLessonText(String filePath) {
        try {
            String rawText = Files.readString(Path.of(filePath));
            Lesson lesson = LessonParser.parseLessonText(rawText);
            engine = new TutorialEngine(lesson);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load lesson file: " + e.getMessage());
            engine = null;
            return false;
        }
    }

    /**
     * Loads multiple lessons out of a text archive file[cite: 24].
     *
     * @param filePath File path string[cite: 24].
     * @return Count of lessons loaded, or -1 on failure[cite: 24].
     */
    public int loadLessonFile(String filePath) {
        try {
            String rawText = Files.readString(Path.of(filePath));
            lessons = LessonParser.parseLessonFile(rawText);
            currentLessonIndex = lessons.isEmpty() ? -1 : 0;
            engine = lessons.isEmpty() ? null : new TutorialEngine(lessons.get(0));
            return lessons.size();
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load lesson file: " + e.getMessage());
            lessons = new ArrayList<>();
            currentLessonIndex = -1;
            engine = null;
            return -1;
        }
    }

    public int getLessonCount() {
        return lessons.size();
    }

    public int getCurrentLessonIndex() {
        return currentLessonIndex;
    }

    public boolean selectLesson(int index) {
        if (index < 0 || index >= lessons.size())
            return false;
        currentLessonIndex = index;
        engine = new TutorialEngine(lessons.get(index));
        return true;
    }

    public List<String> getHandForSeat(int seatIdx) {
        if (engine != null)
            return engine.getHandForSeat(seatIdx);
        return new ArrayList<>();
    }

    public boolean isBiddingPhase() {
        return engine != null && engine.isBiddingPhase();
    }

    public int getCurrentBidTurnSeatIndex() {
        if (engine != null)
            return engine.getCurrentBidTurnSeatIndex();
        return -1;
    }

    public String getExpectedBidType() {
        PlayerAction expected = (engine != null) ? engine.getExpectedBidAction() : null;
        if (expected == null) return "";
        if (expected instanceof ContractBid) return "CONTRACT";
        if (expected instanceof PassAction) return "PASS";
        if (expected instanceof DoubleAction) return "DOUBLE";
        if (expected instanceof RedoubleAction) return "REDOUBLE";
        return "";
    }

    public int getExpectedBidLevel() {
        PlayerAction expected = (engine != null) ? engine.getExpectedBidAction() : null;
        return (expected instanceof ContractBid cb) ? cb.getLevel() : 0;
    }

    public String getExpectedBidStrain() {
        PlayerAction expected = (engine != null) ? engine.getExpectedBidAction() : null;
        return (expected instanceof ContractBid cb) ? cb.getStrain().name() : "";
    }

    public boolean submitBid(int seatIdx, int level, String strainName) {
        if (engine == null || strainName == null)
            return false;
        Strain strain;
        try {
            strain = Strain.valueOf(strainName);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (level < 1 || level > 7)
            return false;
        return engine.submitBidAction(seatIdx, new ContractBid(level, strain));
    }

    public boolean submitPass(int seatIdx) {
        return engine != null && engine.submitBidAction(seatIdx, new PassAction());
    }

    public boolean submitDouble(int seatIdx) {
        return engine != null && engine.submitBidAction(seatIdx, new DoubleAction());
    }

    public boolean submitRedouble(int seatIdx) {
        return engine != null && engine.submitBidAction(seatIdx, new RedoubleAction());
    }

    public boolean isAwaitingClaimConcede() {
        return (engine != null) && engine.isAwaitingClaimConcede();
    }
}