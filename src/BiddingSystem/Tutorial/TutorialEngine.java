package BiddingSystem.Tutorial;

import LessonTutorial.Lesson;
import LessonTutorial.LessonOutcome;
import java.util.*;
import logic.*;

public class TutorialEngine {
    private Lesson lesson;
    private PlayerPosition leaderSeat;
    private int currentTrickIdx = 0;
    private int mistakeCount = 0;
    private int currentPlayInTrick = 0;
    private LessonOutcome finalOutcome = null;
    private boolean isAutoComplete = false;

    public TutorialEngine(Lesson lesson) {
        this.lesson = lesson;
        // get the player who starts the first trick
        PlayerPosition openingLeader = lesson.getOpeningLeader();
        if (openingLeader != null)
            this.leaderSeat = openingLeader;
        else // sets West as default if openingLeader is null
            this.leaderSeat = PlayerPosition.WEST;
    }

    public LessonOutcome getFinalOutcome() {
        // MERGE NOTE: was `finalOutcome()` (calling a nonexistent method) -
        // finalOutcome is the field declared above, not a methodfixed the typo
        if (finalOutcome != null)
            return finalOutcome;
        else
            return lesson.outcome;
    }

    // get the number of mistakes
    public int getMistakeCount() {
        return mistakeCount;
    }

    // get the lesson note
    public String getLessonNote() {
        return lesson.note;
    }

    // checks if all tricks have been played
    public boolean isTutorialComplete() {
        return (isAutoComplete || currentTrickIdx >= lesson.tricks.size());
    }

    // get card to be played next
    public String getExpectedCardCode() {
        if (isTutorialComplete())
            return null;
        Card card = lesson.tricks.get(currentTrickIdx).get(currentPlayInTrick);
        String suit = card.getSuit().getSuitLetter();
        String rank = card.getRank().getRankLetter();
        return suit+rank;
    }

    // number of the current player's seat
    public int getCurrentTurnSeatIndex() {
        PlayerPosition seat = getCurrentTurnSeat();
        if (seat == null)
            return -1;
        return seat.ordinal();
    }

    // checks if the card is played correctly
    public boolean playCard(int seatIdx, String cardCode) {
        if (isTutorialComplete())
            return false;

        // check it's the right player playing
        if (seatIdx != getCurrentTurnSeatIndex())
            return false; // trying to play out-of-turn
        String expectedCode = getExpectedCardCode();

        if (expectedCode != null) {
            if (expectedCode.equalsIgnoreCase(cardCode)) {
                advanceStep(); // the correct card was played (matches ideal game)
                return true;
            }
        }
        mistakeCount++; // wrong card played
        return false;
    }

    // Moving to the next card in the lesson
    private void advanceStep() {
        currentPlayInTrick++;
        if (currentPlayInTrick == 4) {
            currentPlayInTrick = 0;
            List<Card> cards = lesson.tricks.get(currentTrickIdx); // who won the trick
            leaderSeat = calculateTrickWinner(cards);
            currentTrickIdx++;
        }

        if (currentTrickIdx >= lesson.tricks.size() && lesson.outcome != null) {
            this.isAutoComplete = true;
            this.finalOutcome = lesson.outcome;
        }
    }

    // using PlayValidation.pickWinner with lesson.trumpSuit
    private PlayerPosition calculateTrickWinner(List<Card> cards) {
        if (cards == null || cards.isEmpty())
            return leaderSeat;
        
        Trick trick = new Trick(leaderSeat);
        PlayerPosition seat = leaderSeat;
        for (Card card: cards) {
            trick.recordPlay(seat, card);
            seat = seat.next();
        }
        return PlayValidation.pickWinner(trick, lesson.trumpSuit);
    }

    public PlayerPosition getCurrentTurnSeat() {
        if (isTutorialComplete()) {
            return null;
        }

        int leader = leaderSeat.ordinal();
        int player = ((leader+currentPlayInTrick)%4);
        return PlayerPosition.values()[player];
    }

    // user claims all the remaining tricks
    public boolean claim() {
        if (isTutorialComplete())
            return false;

        if (lesson.outcome == LessonOutcome.CLAIM) { // does the lesson text expect a Claim
            isAutoComplete = true;
            finalOutcome = LessonOutcome.CLAIM;
            return true;
        }
        else { // player claimed when the lesson text expects otherwise
            mistakeCount++;
            return false;
        }
    }

    // user concedes all the remaining tricks
    public boolean concede() {
        if (isTutorialComplete())
            return false;

        if (lesson.outcome == LessonOutcome.CONCEDE) { // does the lesson text expect a concede
            isAutoComplete = true;
            finalOutcome = LessonOutcome.CONCEDE;
            return true;
        }
        else { // player conceded when the lesson text expects otherwise
            mistakeCount++;
            return false;
        }
    }
}
