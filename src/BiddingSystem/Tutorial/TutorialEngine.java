package BiddingSystem.Tutorial;

import BiddingSystem.BiddingData.Actions.*;
import BiddingSystem.BiddingLogic.BiddingManager;
import BiddingSystem.Player;
import LessonTutorial.Lesson;
import LessonTutorial.LessonOutcome;
import java.util.*;
import logic.*;

/**
 * Core engine governing step-by-step interactive execution of a Bridge tutorial lesson[cite: 23].
 * Tracks bidding, scripted play sequences, mistake counts, and claim/concede validation[cite: 23].
 */
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

    /**
     * Initializes the engine with a parsed lesson[cite: 23].
     *
     * @param lesson The {@link Lesson} object containing hands, bidding scripts, or play sequences[cite: 23, 25].
     */
    public TutorialEngine(Lesson lesson) {
        this.lesson = lesson;
        if (lesson.isBidAndPlayMode()) {
            this.biddingPhase = true;
            this.biddingManager = new BiddingManager(lesson.dealer, buildPlayers(lesson));
        } else {
            this.biddingPhase = false;
            startPlayPhase();
        }
    }

    /**
     * Gets the final outcome of the lesson (e.g., CLAIM or CONCEDE)[cite: 23, 26].
     *
     * @return The {@link LessonOutcome}[cite: 23, 26].
     */
    public LessonOutcome getFinalOutcome() {
        if (finalOutcome != null)
            return finalOutcome;
        else
            return lesson.outcome;
    }

    /**
     * Gets total mistakes made by the user during this lesson[cite: 23].
     *
     * @return Mistake count[cite: 23].
     */
    public int getMistakeCount() {
        return mistakeCount;
    }

    /**
     * Gets the pedagogical note attached to the lesson[cite: 23].
     *
     * @return Note string[cite: 23].
     */
    public String getLessonNote() {
        return lesson.note;
    }

    /**
     * Checks if the tutorial has been completed[cite: 23].
     *
     * @return {@code true} if completed; {@code false} otherwise[cite: 23].
     */
    public boolean isTutorialComplete() {
        return isAutoComplete;
    }

    private boolean hasMoreScriptedCards() {
        return currentTrickIdx < lesson.tricks.size();
    }

    /**
     * Checks if all scripted card plays are done and the user needs to claim or concede[cite: 23].
     *
     * @return {@code true} if waiting for claim/concede; {@code false} otherwise[cite: 23].
     */
    public boolean isAwaitingClaimConcede() {
        return !isAutoComplete && !hasMoreScriptedCards();
    }

    /**
     * Checks if the lesson is currently in the bidding phase[cite: 23].
     *
     * @return {@code true} if bidding; {@code false} if in card play phase[cite: 23].
     */
    public boolean isBiddingPhase() {
        return biddingPhase;
    }

    /**
     * Gets the 2-character card code expected for the next move[cite: 23].
     *
     * @return Expected card code string (e.g., "S8"), or {@code null} if none[cite: 23].
     */
    public String getExpectedCardCode() {
        if (isTutorialComplete() || !hasMoreScriptedCards())
            return null;
        Card card = lesson.tricks.get(currentTrickIdx).get(currentPlayInTrick);
        String suit = card.getSuit().getSuitLetter();
        String rank = card.getRank().getRankLetter();
        return suit + rank;
    }

    /**
     * Gets the seat position whose turn it is to play[cite: 23].
     *
     * @return Active {@link PlayerPosition}, or {@code null} if complete[cite: 23].
     */
    public PlayerPosition getCurrentTurnSeat() {
        if (isTutorialComplete() || !hasMoreScriptedCards()) {
            return null;
        }

        int leader = leaderSeat.ordinal();
        int player = ((leader + currentPlayInTrick) % 4);
        return PlayerPosition.values()[player];
    }

    /**
     * Gets the integer seat index of the current turn[cite: 23].
     *
     * @return Seat index (0=SOUTH, 1=WEST, 2=NORTH, 3=EAST), or -1 if complete[cite: 23].
     */
    public int getCurrentTurnSeatIndex() {
        PlayerPosition seat = getCurrentTurnSeat();
        if (seat == null)
            return -1;
        return seat.ordinal();
    }

    /**
     * Gets the seat index of the trick leader[cite: 23].
     *
     * @return Leader seat index[cite: 23].
     */
    public int getLeaderSeatIndex() {
        if (leaderSeat != null)
            return leaderSeat.ordinal();
        return -1;
    }

    /**
     * Validates and executes a card play for a seat[cite: 23].
     *
     * @param seatIdx  Attempting seat index[cite: 23].
     * @param cardCode Card code played[cite: 23].
     * @return {@code true} if matched script; {@code false} if wrong card or out of turn[cite: 23].
     */
    public boolean playCard(int seatIdx, String cardCode) {
        if (isTutorialComplete() || !hasMoreScriptedCards())
            return false;

        if (seatIdx != getCurrentTurnSeatIndex())
            return false;
        String expectedCode = getExpectedCardCode();

        if (expectedCode != null) {
            if (expectedCode.equalsIgnoreCase(cardCode)) {
                advanceStep();
                return true;
            }
        }
        mistakeCount++;
        return false;
    }

    private void advanceStep() {
        currentPlayInTrick++;
        if (currentPlayInTrick == 4) {
            currentPlayInTrick = 0;
            List<Card> cards = lesson.tricks.get(currentTrickIdx);
            leaderSeat = calculateTrickWinner(cards);
            currentTrickIdx++;
        }
    }

    private PlayerPosition calculateTrickWinner(List<Card> cards) {
        if (cards == null || cards.isEmpty())
            return leaderSeat;

        Trick trick = new Trick(leaderSeat);
        PlayerPosition seat = leaderSeat;
        for (Card card : cards) {
            trick.recordPlay(seat, card);
            seat = seat.next();
        }
        return PlayValidation.pickWinner(trick, lesson.trumpSuit);
    }

    /**
     * Retrieves card codes dealt to a specific seat for the lesson[cite: 23].
     *
     * @param seatIdx Seat index[cite: 23].
     * @return List of card codes[cite: 23].
     */
    public List<String> getHandForSeat(int seatIdx) {
        List<String> cardCodes = new ArrayList<>();
        if (lesson == null || seatIdx < 0 || seatIdx >= 4)
            return cardCodes;

        PlayerPosition seat = PlayerPosition.values()[seatIdx];
        List<Card> hand = lesson.getHandForSeat(seat);

        if (hand != null) {
            for (Card card : hand) {
                cardCodes.add(card.getSuit().getSuitLetter() + card.getRank().getRankLetter());
            }
        }
        return cardCodes;
    }

    /**
     * Validates a Claim action[cite: 23].
     *
     * @return {@code true} if claim was expected at this step; {@code false} otherwise[cite: 23].
     */
    public boolean claim() {
        if (isTutorialComplete())
            return false;

        boolean allTricksPlayed = !hasMoreScriptedCards();

        if (allTricksPlayed && lesson.outcome == LessonOutcome.CLAIM) {
            isAutoComplete = true;
            finalOutcome = LessonOutcome.CLAIM;
            return true;
        } else {
            mistakeCount++;
            return false;
        }
    }

    /**
     * Validates a Concede action[cite: 23].
     *
     * @return {@code true} if concede was expected at this step; {@code false} otherwise[cite: 23].
     */
    public boolean concede() {
        if (isTutorialComplete())
            return false;

        boolean allTricksPlayed = (currentTrickIdx >= lesson.tricks.size());

        if (allTricksPlayed && lesson.outcome == LessonOutcome.CONCEDE) {
            isAutoComplete = true;
            finalOutcome = LessonOutcome.CONCEDE;
            return true;
        } else {
            mistakeCount++;
            return false;
        }
    }

    private void startPlayPhase() {
        if (biddingManager != null) {
            lesson.declarer = biddingManager.getCurrentDeclarer();
            ContractBid winningBid = biddingManager.getCurrentContractBid();
            if (winningBid != null) {
                lesson.trumpSuit = winningBid.getStrain().toSuit();
            }
        }
        PlayerPosition openingLeader = lesson.getOpeningLeader();
        if (openingLeader != null)
            this.leaderSeat = openingLeader;
        else
            this.leaderSeat = PlayerPosition.WEST;
    }

    private static Player[] buildPlayers(Lesson lesson) {
        Player south = new Player("South", new PlayerHand(PlayerPosition.SOUTH), PlayerPosition.SOUTH);
        Player west = new Player("West", new PlayerHand(PlayerPosition.WEST), PlayerPosition.WEST);
        Player north = new Player("North", new PlayerHand(PlayerPosition.NORTH), PlayerPosition.NORTH);
        Player east = new Player("East", new PlayerHand(PlayerPosition.EAST), PlayerPosition.EAST);
        return new Player[]{south, west, north, east};
    }

    public int getCurrentBidTurnSeatIndex() {
        if (!biddingPhase) return -1;
        return biddingManager.getCurrentPlayer().getSeatPosition().ordinal();
    }

    public PlayerAction getExpectedBidAction() {
        if (!biddingPhase || bidIdx >= lesson.rawAuction.size()) return null;
        return lesson.rawAuction.get(bidIdx);
    }

    public boolean submitBidAction(int seatIdx, PlayerAction submitted) {
        if (!biddingPhase || seatIdx != getCurrentBidTurnSeatIndex()) return false;
        PlayerAction expected = getExpectedBidAction();
        if (expected != null && bidsMatch(expected, submitted)) {
            biddingManager.ActionPlayed(submitted);
            advanceBid();
            return true;
        }
        mistakeCount++;
        return false;
    }

    public boolean bidsMatch(PlayerAction expected, PlayerAction submitted) {
        if (expected.getClass() == submitted.getClass()) {
            if (expected instanceof ContractBid) {
                return (expected.getStrain() == submitted.getStrain()) && (expected.getLevel() == submitted.getLevel());
            } else if (expected instanceof PassAction || expected instanceof DoubleAction || expected instanceof RedoubleAction) {
                return true;
            }
        }
        return false;
    }

    public void advanceBid() {
        bidIdx++;
        if (bidIdx >= lesson.rawAuction.size()) {
            biddingPhase = false;
            startPlayPhase();
        }
    }
}