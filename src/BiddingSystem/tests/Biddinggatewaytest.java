package BiddingSystem.tests;


import BiddingSystem.BiddingGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * (AI GENERATED)
 * Tests BiddingGateway directly (plain Java method calls, no live Py4J socket
 * connection) — this verifies the translation/delegation logic the gateway
 * adds on top of BiddingManager, which is the actual risk surface introduced
 * during integration.
 *
 * NOTE: BiddingGateway currently deals a fresh game internally in its
 * constructor (freshGame()), so each test gets an independent, randomly
 * dealt game — bid legality tests below don't depend on hand contents.
 */
class BiddingGatewayTest {

    private BiddingGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new BiddingGateway();
    }

    // ---------- submitBid: valid input ----------

    @Test
    void submitBidAcceptsLegalOpeningBid() {
        assertTrue(gateway.submitBid(2, "HEARTS"));
    }

    @Test
    void submitBidAcceptsEveryStrainName() {
        String[] strains = { "CLUBS", "DIAMONDS", "HEARTS", "SPADES", "NO_TRUMP" };
        for (String strain : strains) {
            BiddingGateway g = new BiddingGateway();
            assertTrue(g.submitBid(1, strain), strain + " should be accepted as a legal opening bid");
        }
    }

    @Test
    void submitBidRejectsLowerLevelAfterHigherBid() {
        gateway.submitBid(3, "CLUBS");
        assertFalse(gateway.submitBid(1, "SPADES"));
    }

    @Test
    void submitBidRejectsSameLevelLowerStrain() {
        gateway.submitBid(2, "HEARTS");
        assertFalse(gateway.submitBid(2, "CLUBS"));
    }

    // ---------- submitBid: malformed input from the Python side ----------

    @Test
    void submitBidRejectsUnknownStrainNameWithoutThrowing() {
        assertDoesNotThrow(() -> {
            boolean result = gateway.submitBid(1, "GARBAGE");
            assertFalse(result);
        });
    }

    @Test
    void submitBidRejectsLevelBelowRange() {
        assertFalse(gateway.submitBid(0, "HEARTS"));
    }

    @Test
    void submitBidRejectsLevelAboveRange() {
        assertFalse(gateway.submitBid(8, "HEARTS"));
    }

    @Test
    void submitBidRejectsNullStrainNameWithoutThrowing() {
        assertDoesNotThrow(() -> {
            boolean result = gateway.submitBid(2, null);
            assertFalse(result);
        });
    }

    // ---------- submitPass ----------

    @Test
    void submitPassAlwaysAccepted() {
        assertTrue(gateway.submitPass());
        gateway.submitBid(2, "HEARTS");
        assertTrue(gateway.submitPass());
    }

    // ---------- turn tracking ----------

    @Test
    void currentSeatIndexAdvancesAfterLegalAction() {
        int firstSeat = gateway.getCurrentSeatIndex();
        gateway.submitPass();
        int secondSeat = gateway.getCurrentSeatIndex();
        assertNotEquals(firstSeat, secondSeat, "Seat index should advance after a legal action");
    }

    @Test
    void currentSeatIndexDoesNotAdvanceAfterRejectedAction() {
        gateway.submitBid(3, "CLUBS");
        int seatBeforeRejected = gateway.getCurrentSeatIndex();
        gateway.submitBid(1, "SPADES"); // illegal, should be rejected
        assertEquals(seatBeforeRejected, gateway.getCurrentSeatIndex());
    }

    @Test
    void currentPlayerNameIsNonNullAndNonEmpty() {
        String name = gateway.getCurrentPlayerName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    // ---------- auction-end detection ----------

    @Test
    void threePassesAfterBidEndsAuctionAsContractFinal() {
        gateway.submitBid(1, "CLUBS");
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        assertTrue(gateway.checkBiddingOver());
        assertFalse(gateway.isPassedOut());
    }

    @Test
    void fourPassesFromStartTriggersPassedOut() {
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        assertTrue(gateway.checkBiddingOver());
        assertTrue(gateway.isPassedOut());
    }

    @Test
    void auctionNotOverAfterOnlyTwoPasses() {
        gateway.submitBid(1, "CLUBS");
        gateway.submitPass();
        gateway.submitPass();
        assertFalse(gateway.checkBiddingOver());
    }

    // ---------- declarer / contract reporting ----------

    @Test
    void winningContractStringReflectsFinalBid() {
        gateway.submitBid(2, "HEARTS");
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        assertTrue(gateway.checkBiddingOver());
        String contract = gateway.getWinningContractString();
        assertTrue(contract.contains("2"));
        assertTrue(contract.contains("HEARTS"));
    }

    @Test
    void declarerNameNonNullAfterContractFinalAuction() {
        gateway.submitBid(1, "SPADES");
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        assertTrue(gateway.checkBiddingOver());
        assertNotNull(gateway.getDeclarerName());
    }

    @Test
    void declarerSeatIndexWithinValidRange() {
        gateway.submitBid(1, "SPADES");
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        assertTrue(gateway.checkBiddingOver());
        int seat = gateway.getDeclarerSeatIndex();
        assertTrue(seat >= 0 && seat <= 3);
    }

    // ---------- passed-out reset ----------

    @Test
    void resetAfterPassedOutReturnsValidSeatIndex() {
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        gateway.checkBiddingOver();
        assertTrue(gateway.isPassedOut());

        int newSeat = gateway.resetAfterPassedOut();
        assertTrue(newSeat >= 0 && newSeat <= 3);
    }

    @Test
    void gatewayUsableAfterPassedOutReset() {
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        gateway.submitPass();
        gateway.resetAfterPassedOut();

        // Confirms the internal manager reference was actually replaced,
        // not just that reset ran without throwing.
        assertTrue(gateway.submitPass());
    }

    // ---------- hand retrieval ----------

    @Test
    void everySeatHasExactlyThirteenCards() {
        for (int seat = 0; seat <= 3; seat++) {
            List<String> hand = gateway.getHandForSeat(seat);
            assertEquals(13, hand.size(), "Seat " + seat + " should have 13 cards");
        }
    }

    @Test
    void allFiftyTwoCardsAreUniqueAcrossAllHands() {
        List<String> all = new java.util.ArrayList<>();
        for (int seat = 0; seat <= 3; seat++) {
            all.addAll(gateway.getHandForSeat(seat));
        }
        assertEquals(52, all.size());
        assertEquals(52, new java.util.HashSet<>(all).size(), "No duplicate cards should exist across all hands");
    }

    @Test
    void cardCodesAreWellFormed() {
        List<String> hand = gateway.getHandForSeat(0);
        for (String code : hand) {
            assertTrue(code.matches("[CDHS](2|3|4|5|6|7|8|9|10|J|Q|K|A)"),
                "Card code '" + code + "' does not match expected suit-letter+rank-letter format");
        }
    }
}
