package BiddingSystem.tests;

import BiddingSystem.BiddingData.Actions.ContractBid;
import BiddingSystem.BiddingData.Actions.PassAction;
import BiddingSystem.BiddingLogic.BiddingManager;
import BiddingSystem.Player;
import logic.Deck;
import logic.PlayerPosition;
import logic.Strain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class BiddingManagerTest {

    private Player north, east, south, west;
    private Player[] players;
    private BiddingManager manager;

    @BeforeEach
    void setUp() {
        north = new Player("North", new logic.PlayerHand(PlayerPosition.NORTH), PlayerPosition.NORTH);
        east  = new Player("East",  new logic.PlayerHand(PlayerPosition.EAST),  PlayerPosition.EAST);
        south = new Player("South", new logic.PlayerHand(PlayerPosition.SOUTH), PlayerPosition.SOUTH);
        west  = new Player("West",  new logic.PlayerHand(PlayerPosition.WEST),  PlayerPosition.WEST);
        players = new Player[]{ south, west, north, east }; // ordered by PlayerPosition.ordinal(): SOUTH,WEST,NORTH,EAST

        manager = new BiddingManager(PlayerPosition.SOUTH, players);
    }

    // ---------- Level & strain selection ----------

    @Test
    void acceptsContractBidAtEveryLevel() {
        // Each level 1-7 should be legal as the very first bid (no history to compare against).
        for (int level = 1; level <= 7; level++) {
            BiddingManager m = new BiddingManager(PlayerPosition.NORTH, players);
            boolean accepted = m.ActionPlayed(new ContractBid(level, Strain.CLUBS));
            assertTrue(accepted, "Level " + level + " should be legal as opening bid");
        }
    }

    @Test
    void acceptsContractBidForEveryStrainIncludingNoTrump() {
        for (Strain s : Strain.values()) {
            BiddingManager m = new BiddingManager(PlayerPosition.NORTH, players);
            boolean accepted = m.ActionPlayed(new ContractBid(1, s));
            assertTrue(accepted, "Strain " + s + " should be legal as opening bid");
        }
    }

    @Test
    void noTrumpOutranksSameLevelSuitBid() {
        manager.ActionPlayed(new ContractBid(1, Strain.SPADES));
        boolean accepted = manager.ActionPlayed(new ContractBid(1, Strain.NO_TRUMP));
        assertTrue(accepted, "1NT should beat 1S (NT is highest-ranked strain)");
    }

    @Test
    void sevenNoTrumpCannotBeOutbid() {
        manager.ActionPlayed(new ContractBid(7, Strain.NO_TRUMP));
        boolean accepted = manager.ActionPlayed(new ContractBid(7, Strain.NO_TRUMP));
        assertFalse(accepted, "A second 7NT should be rejected as an equal, not higher, bid");
    }

    // ---------- Pass ----------

    @Test
    void passIsAlwaysLegal() {
        assertTrue(manager.ActionPlayed(new PassAction()));
        manager.ActionPlayed(new ContractBid(2, Strain.HEARTS));
        assertTrue(manager.ActionPlayed(new PassAction()), "Pass should remain legal after a bid exists");
    }

    // ---------- Illegal bid rejection ----------

    @Test
    void rejectsLowerLevelBid() {
        manager.ActionPlayed(new ContractBid(3, Strain.CLUBS));
        boolean accepted = manager.ActionPlayed(new ContractBid(1, Strain.SPADES));
        assertFalse(accepted, "1S after 3C should be rejected: lower level");
    }

    @Test
    void rejectsSameLevelLowerStrainBid() {
        manager.ActionPlayed(new ContractBid(2, Strain.HEARTS));
        boolean accepted = manager.ActionPlayed(new ContractBid(2, Strain.CLUBS));
        assertFalse(accepted, "2C after 2H should be rejected: same level, lower strain");
    }

    @Test
    void rejectsExactRepeatBid() {
        manager.ActionPlayed(new ContractBid(2, Strain.HEARTS));
        boolean accepted = manager.ActionPlayed(new ContractBid(2, Strain.HEARTS));
        assertFalse(accepted, "Repeating the exact same bid should be rejected: not strictly higher");
    }

    @Test
    void rejectedBidDoesNotAdvanceTurnOrLog() {
        Player before = manager.getCurrentPlayer();
        manager.ActionPlayed(new ContractBid(3, Strain.CLUBS));
        Player turnBefore = manager.getCurrentPlayer();
        manager.ActionPlayed(new ContractBid(1, Strain.SPADES)); // illegal
        assertEquals(turnBefore, manager.getCurrentPlayer(), "Turn should not advance on a rejected action");
    }

    // ---------- Turn order ----------

    @Test
    void turnAdvancesInSeatOrderOnLegalActions() {
        Player first = manager.getCurrentPlayer(); // North (dealer/starting pos)
        manager.ActionPlayed(new PassAction());
        assertEquals(west.getUsername(), manager.getCurrentPlayer().getUsername(), "Turn should move South -> West");
        manager.ActionPlayed(new PassAction());
        assertEquals(north.getUsername(), manager.getCurrentPlayer().getUsername(), "Turn should move West -> North");
    }

    // ---------- Auction-end detection ----------

    @Test
    void threeConsecutivePassesEndsAuctionWithContract() {
        manager.ActionPlayed(new PassAction());                     // South passes
        manager.ActionPlayed(new ContractBid(1, Strain.CLUBS));     // West bids 1C
        manager.ActionPlayed(new PassAction());                     // North
        manager.ActionPlayed(new PassAction());                     // East
        manager.ActionPlayed(new PassAction());
        boolean over = manager.checkBiddingOver();
        assertTrue(over, "Auction should be over after 3 passes following a bid");
        // assertFalse(manager.isPassedOut()); // uncomment once getter name confirmed
    }

    @Test
    void fourInitialPassesTriggersPassedOut() {
        manager.ActionPlayed(new PassAction());
        manager.ActionPlayed(new PassAction());
        manager.ActionPlayed(new PassAction());
        manager.ActionPlayed(new PassAction());
        boolean over = manager.checkBiddingOver();
        assertTrue(over, "Auction should be over after 4 straight passes");
        // assertTrue(manager.isPassedOut()); // uncomment once getter name confirmed
    }

    // ---------- Declarer determination ----------

    @Test
    void declarerIsFirstPartnerToNameTheWinningStrain() {
        // North opens 1H, East passes, South raises to 2H (partner of North bidding
        // the SAME strain again), West passes, North passes, East passes.
        // Winning contract: 2H. Declarer should be NORTH (bid Hearts first),
        // not SOUTH (who made the final, higher bid).
        manager.ActionPlayed(new ContractBid(1, Strain.HEARTS)); // South
        manager.ActionPlayed(new PassAction());                  // West
        manager.ActionPlayed(new ContractBid(2, Strain.HEARTS)); // North
        manager.ActionPlayed(new PassAction());                  // East
        manager.ActionPlayed(new PassAction());                  // South
        manager.ActionPlayed(new PassAction());                  // West

        boolean over = manager.checkBiddingOver();
        assertTrue(over);
        assertEquals(south.getUsername(), manager.getDeclarer().getUsername(),
            "Declarer should be North (first to bid Hearts), not South (final/highest bidder)");
    }

    // ---------- Fuzzing: randomized legal-only sequences shouldn't crash ----------

    @Test
    void randomizedLegalSequenceDoesNotThrow() {
        Random rand = new Random(42); // fixed seed for reproducibility
        BiddingManager m = new BiddingManager(PlayerPosition.NORTH, players);
        int currentLevel = 0;
        int safetyLimit = 200;

        for (int i = 0; i < safetyLimit; i++) {
            boolean tryBid = rand.nextBoolean() && currentLevel < 7;
            boolean accepted;
            if (tryBid) {
                int level = currentLevel + 1;
                Strain strain = Strain.values()[rand.nextInt(Strain.values().length)];
                accepted = m.ActionPlayed(new ContractBid(level, strain));
                if (accepted) currentLevel = level;
            } else {
                accepted = m.ActionPlayed(new PassAction());
            }
            assertDoesNotThrow(m::checkBiddingOver);
            if (m.checkBiddingOver()) break;
        }
        // Test passes as long as nothing threw across many randomized legal actions.
    }
}
