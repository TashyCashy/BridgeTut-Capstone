package BiddingSystem.Tutorial;

import LessonTutorial.Lesson;

public class TutorialGateway {
    private final TutorialEngine engine;

    public TutorialGateway(TutorialEngine engine) {
        this.engine = engine;
    }

    public int getCurrentTurnSeatIndex() {
        return engine.getCurrentTurnSeatIndex();
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

    public String getLessonNote() {
        return engine.getLessonNote();
    }
}