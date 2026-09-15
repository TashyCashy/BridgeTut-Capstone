package BiddingSystem.Tutorial;

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
}