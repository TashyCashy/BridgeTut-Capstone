package BiddingSystem.Tutorial;

import BiddingSystem.BiddingData.Actions.*;
import BiddingSystem.BiddingLogic.BiddingManager;
import BiddingSystem.Player;
import LessonTutorial.Lesson;
import LessonTutorial.LessonOutcome;
import java.util.*;
import logic.*;
import BiddingSystem.*;

public class TutorialEngine {
    private Lesson lesson;
    private PlayerPosition leaderSeat;
    private int currentTrickIdx = 0;
    private int mistakeCount = 0;
    private int currentPlayInTrick = 0;
    private LessonOutcome finalOutcome = null;
    private boolean isAutoComplete = false;
    private boolean biddingPhase;
    private BiddingManager biddingManager;
    private int bidIdx = 0;

    public TutorialEngine(Lesson lesson) {
        this.lesson = lesson;
        if (lesson.isBidAndPlayMode()){
            //for mode 1
            this.biddingPhase = true;
            this.biddingManager = new BiddingManager(lesson.dealer, buildPlayers(lesson));
        }
        else {
            this.biddingPhase = false;
            startPlayPhase();
        }
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

    // checks if the lesson is still in its bidding phase (mode 1 only)
    public boolean isBiddingPhase() {
        return biddingPhase;
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

    // name of the current player
    public PlayerPosition getCurrentTurnSeat() {
        if (isTutorialComplete()) {
            return null;
        }

        int leader = leaderSeat.ordinal();
        int player = ((leader+currentPlayInTrick)%4);
        return PlayerPosition.values()[player];
    }

    // number of the current player's seat
    public int getCurrentTurnSeatIndex() {
        PlayerPosition seat = getCurrentTurnSeat();
        if (seat == null)
            return -1;
        return seat.ordinal();
    }

    // number of the leader's seat for winner checks
    public int getLeaderSeatIndex() {
        if (leaderSeat != null)
            return leaderSeat.ordinal();
        return -1;
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

    // gets a list of card codes for the specified seat based on the current lesson
    public List<String> getHandForSeat(int seatIdx) {
        List<String> cardCodes = new ArrayList<>();
        if (lesson == null || seatIdx < 0 || seatIdx >= 4)
            return cardCodes;

        PlayerPosition seat = PlayerPosition.values()[seatIdx];
        List<Card> hand = lesson.getHandForSeat(seat);

        if (hand != null) {
            for (Card card: hand) {
                cardCodes.add(card.getSuit().getSuitLetter() + card.getRank().getRankLetter());
            }
        }
        return cardCodes;
    }

    // user claims all the remaining tricks
    public boolean claim() {
        if (isTutorialComplete())
            return false;

        // only allow claim/concede after all listed tricks in the lesson are played
        boolean allTricksPlayed = (currentTrickIdx >= lesson.tricks.size());

        if (allTricksPlayed && lesson.outcome == LessonOutcome.CLAIM) { // does the lesson text expect a Claim
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

        // only allow claim/concede after all listed tricks in the lesson are played
        boolean allTricksPlayed = (currentTrickIdx >= lesson.tricks.size());

        if (allTricksPlayed && lesson.outcome == LessonOutcome.CONCEDE) { // does the lesson text expect a concede
            isAutoComplete = true;
            finalOutcome = LessonOutcome.CONCEDE;
            return true;
        }
        else { // player conceded when the lesson text expects otherwise
            mistakeCount++;
            return false;
        }
    }

    private void startPlayPhase (){
        if (biddingManager != null){
            lesson.declarer = biddingManager.getCurrentDeclarer();
            ContractBid winningBid = biddingManager.getCurrentContractBid();
            if(winningBid != null){
                lesson.trumpSuit = winningBid.getStrain().toSuit();
            }
        }
        // get the player who starts the first trick
        PlayerPosition openingLeader = lesson.getOpeningLeader();
        if (openingLeader != null)
            this.leaderSeat = openingLeader;
        else // sets West as default if openingLeader is null
            this.leaderSeat = PlayerPosition.WEST;
    }

    private static Player[] buildPlayers(Lesson lesson){
        Player south = new Player("South", new PlayerHand(PlayerPosition.SOUTH), PlayerPosition.SOUTH);
        Player west  = new Player("West",  new PlayerHand(PlayerPosition.WEST),  PlayerPosition.WEST);
        Player north = new Player("North", new PlayerHand(PlayerPosition.NORTH), PlayerPosition.NORTH);
        Player east  = new Player("East",  new PlayerHand(PlayerPosition.EAST),  PlayerPosition.EAST);
        // order MUST match PlayerPosition.values(): SOUTH, WEST, NORTH, EAST
        return new Player[]{south, west, north, east};
    }

    public int getCurrentBidTurnSeatIndex(){
        if (!biddingPhase) return -1;
        return biddingManager.getCurrentPlayer().getSeatPosition().ordinal();
    }

    public PlayerAction getExpectedBidAction(){
      if (!biddingPhase || bidIdx >= lesson.rawAuction.size()) return null;
      return lesson.rawAuction.get(bidIdx);
}

public boolean submitBidAction (int seatIdx, PlayerAction submitted){
        if (!biddingPhase || seatIdx != getCurrentBidTurnSeatIndex()) return false;
        PlayerAction expected = getExpectedBidAction();
        if (expected != null && bidsMatch(expected, submitted)){
            biddingManager.ActionPlayed(submitted);
            advanceBid();
            return true;
        }
        mistakeCount++;
        return false;
}

public boolean bidsMatch (PlayerAction expected, PlayerAction submitted){
        if (expected.getClass() ==  submitted.getClass()){
            if (expected instanceof ContractBid){
                if ((expected.getStrain() == submitted.getStrain()) && (expected.getLevel() == submitted.getLevel())){
                    return true;
                }
                else{
                    return false; //not equal contract bids
                }
            }
            else if (expected instanceof PassAction || expected instanceof DoubleAction || expected instanceof RedoubleAction){
                return true;
            }


            //No need for pass action check, handled with class check
            //no need for double check as well
            //no need for redouble check
        }
        return false; //if not of the same class cant be equal e.g Pass and redouble should return false
}

public void advanceBid (){
        bidIdx++;
        if (bidIdx >= lesson.rawAuction.size()){
            biddingPhase = false;
            startPlayPhase();
        }
}

}
