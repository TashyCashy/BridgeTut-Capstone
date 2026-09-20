package BiddingSystem;

import logic.PlayerPosition;
import BiddingSystem.Tutorial.TutorialGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Complements BiddingSystem.tests.BiddingGatewayTest (submitBid/submitPass/
 * turn-tracking/auction-end/hand-retrieval) with coverage for the rest of
 * BiddingGateway's surface: doubling/redoubling, the live "declarer so far"
 * during an ongoing auction, undo, and the handoff into the tutorial/play
 * phase gateways.
 *
 * BiddingGateway's constructor randomises the starting seat, so every test
 * reads gateway.getCurrentSeatIndex() up front and reasons relative to that
 * seat rather than assuming a fixed one.
 */
class BiddingGatewayTest {

    private BiddingGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new BiddingGateway();
    }

    private PlayerPosition seatAt(int index) {
        return PlayerPosition.values()[index];
    }

    // ---------- doubling ----------

    @Test
    @DisplayName("Double is rejected before any contract bid has been made")
    void submitDoubleRejectedWithNoContractBidYet() {
        assertFalse(gateway.submitDouble());
    }

    @Test
    @DisplayName("Double is rejected when attempted by the bidder's own partnership")
    void submitDoubleRejectedForOwnSide() {
        gateway.submitBid(1, "CLUBS");   // opening bidder's side
        gateway.submitPass();            // opponent passes
        // now it's the opening bidder's partner's turn - same side as the bid
        assertFalse(gateway.submitDouble());
    }

    @Test
    @DisplayName("Double is accepted by the opposing partnership")
    void submitDoubleAcceptedForOpposingSide() {
        gateway.submitBid(1, "CLUBS");
        // it's the very next seat's turn - always an opponent of the bidder
        assertTrue(gateway.submitDouble());
        assertEquals("DOUBLED", gateway.getCurrentDoublingState());
    }

    @Test
    @DisplayName("A second double is rejected while already doubled")
    void submitDoubleRejectedWhenAlreadyDoubled() {
        gateway.submitBid(1, "CLUBS");
        gateway.submitDouble();
        assertFalse(gateway.submitDouble());
    }

    @Test
    @DisplayName("Redouble is rejected while undoubled")
    void submitRedoubleRejectedWhenNotDoubled() {
        gateway.submitBid(1, "CLUBS");
        assertFalse(gateway.submitRedouble());
    }

    @Test
    @DisplayName("Redouble is accepted by the doubled side, rejected by the doubling side")
    void submitRedoubleRespectsPartnership() {
        gateway.submitBid(1, "CLUBS");   // opening bidder's side
        gateway.submitDouble();          // next seat (opponent) doubles - legal
        gateway.submitPass();            // opening bidder's partner passes instead of redoubling
        // the doubler's partner - the doubling side - may not redouble
        assertFalse(gateway.submitRedouble());
        gateway.submitPass();
        // the original bidder - the doubled side - may redouble
        assertTrue(gateway.submitRedouble());
        assertEquals("REDOUBLED", gateway.getCurrentDoublingState());
    }

    // ---------- doubling state reporting ----------

    @Test
    @DisplayName("Doubling state starts UNDOUBLED and getFinalDoublingState reflects it once bidding ends")
    void doublingStateReportedCorrectlyThroughAnAuction() {
        assertEquals("UNDOUBLED", gateway.getCurrentDoublingState());

        gateway.submitBid(1, "CLUBS");
        gateway.submitDouble();
        assertEquals("DOUBLED", gateway.getCurrentDoublingState());

        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();

        assertTrue(gateway.checkBiddingOver());
        assertEquals("DOUBLED", gateway.getFinalDoublingState());
        assertTrue(gateway.getWinningContractString().contains("DOUBLED"));
    }

    // ---------- live declarer during an ongoing auction ----------

    @Test
    @DisplayName("getCurrentDeclarerSeatIndex is empty before any contract bid is made")
    void currentDeclarerEmptyBeforeAnyBid() {
        assertEquals("", gateway.getCurrentDeclarerSeatIndex());
    }

    @Test
    @DisplayName("getCurrentDeclarerSeatIndex tracks whoever bid the current strain first on the winning side")
    void currentDeclarerFollowsFirstBidderOfStrainOnWinningSide() {
        int openerIdx = gateway.getCurrentSeatIndex();
        PlayerPosition opener = seatAt(openerIdx);

        gateway.submitBid(1, "HEARTS");                 // opener bids hearts first
        assertEquals(opener.name(), gateway.getCurrentDeclarerSeatIndex());

        gateway.submitPass();                           // opponent passes
        gateway.submitBid(2, "HEARTS");                  // opener's partner raises hearts

        // opener bid hearts first on this side, so they stay the declarer-so-far
        // even though their partner just made the more recent, higher bid
        assertEquals(opener.name(), gateway.getCurrentDeclarerSeatIndex());
    }

    // ---------- undo ----------

    @Test
    @DisplayName("undoLastBid with no bids made yet returns false without throwing")
    void undoLastBidWithEmptyHistoryReturnsFalse() {
        assertDoesNotThrow(() -> assertFalse(gateway.undoLastBid()));
    }

    @Test
    @DisplayName("undoLastBid reverts the turn to whoever made the undone bid")
    void undoLastBidRevertsTurn() {
        int seatBefore = gateway.getCurrentSeatIndex();
        gateway.submitBid(1, "CLUBS");
        assertNotEquals(seatBefore, gateway.getCurrentSeatIndex());

        assertTrue(gateway.undoLastBid());
        assertEquals(seatBefore, gateway.getCurrentSeatIndex());
    }

    @Test
    @DisplayName("undoLastBid removes the doubled state along with the double itself")
    void undoLastBidRevertsDoublingState() {
        gateway.submitBid(1, "CLUBS");
        gateway.submitDouble();
        assertEquals("DOUBLED", gateway.getCurrentDoublingState());

        gateway.undoLastBid();
        assertEquals("UNDOUBLED", gateway.getCurrentDoublingState());
    }

    // ---------- tutorial gateway ----------

    @Test
    @DisplayName("getTutorialGateway never returns null, and is the same instance on repeated calls")
    void getTutorialGatewayReturnsStableInstance() {
        TutorialGateway first = gateway.getTutorialGateway();
        TutorialGateway second = gateway.getTutorialGateway();
        assertNotNull(first);
        assertSame(first, second);
    }

    // ---------- play-phase handoff ----------

    @Test
    @DisplayName("startPlayPhase hands off a usable PlayingGateway once bidding ends with a contract")
    void startPlayPhaseReturnsUsablePlayingGateway() {
        gateway.submitBid(1, "CLUBS");
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        assertTrue(gateway.checkBiddingOver());
        assertFalse(gateway.isPassedOut());

        PlayingGateway playGateway = gateway.startPlayPhase();

        assertNotNull(playGateway);
        int leaderSeat = playGateway.getCurrentTurnSeatIndex();
        assertTrue(leaderSeat >= 0 && leaderSeat <= 3);
        assertEquals(13, playGateway.getRemainingHandForSeat(leaderSeat).size());
    }
}
