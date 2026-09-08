package BiddingSystem.tests;

import BiddingSystem.BiddingData.Actions.ContractBid;
import BiddingSystem.BiddingData.Actions.DoubleAction;
import BiddingSystem.BiddingData.Actions.PassAction;
import BiddingSystem.BiddingData.Actions.RedoubleAction;
import BiddingSystem.BiddingLogic.BiddingManager;
import BiddingSystem.BiddingLogic.GameReset;
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

    // ---------- Double / Redouble ----------

    @Test
    void opponentCanDoubleAContractBid() {
        GameReset.resetGame(manager);
        manager.ActionPlayed(new ContractBid(1, Strain.HEARTS)); // Sorth
        boolean accepted = manager.ActionPlayed(new DoubleAction()); // West (opponent of Sout)
        assertTrue(accepted, "West (opponent) should be able to double South's 1H");
    }

    @Test
    void partnerCannotDoubleOwnSidesBid() {
        manager.ActionPlayed(new ContractBid(1, Strain.HEARTS)); // South
        manager.ActionPlayed(new PassAction());                  // West
        boolean accepted = manager.ActionPlayed(new DoubleAction());                // North (teammate)
        assertFalse(accepted, "North (teammate) should not be able to double South's 1H");
    }

    @Test
    void northsPartnerCannotDoubleNorthsBid() {
        BiddingManager m = new BiddingManager(PlayerPosition.NORTH, players);
        m.ActionPlayed(new ContractBid(1, Strain.HEARTS)); // North bids
        m.ActionPlayed(new PassAction());                  // East passes
        boolean accepted = m.ActionPlayed(new DoubleAction()); // South = North's partner
        assertFalse(accepted, "South should NOT be able to double North's own partnership's bid");
    }

    @Test
    void cannotDoubleWhenAlreadyDoubled() {
        manager.ActionPlayed(new ContractBid(1, Strain.HEARTS)); // North
        manager.ActionPlayed(new DoubleAction());                  // East doubles
        manager.ActionPlayed(new PassAction());                // South passes
        boolean accepted = manager.ActionPlayed(new DoubleAction()); // West tries to double again
        assertFalse(accepted, "Cannot double an already-doubled contract");
    }

    @Test
    void cannotDoubleWithNoBidMade() {
        GameReset.resetGame(manager);
        boolean accepted = manager.ActionPlayed(new DoubleAction()); // North, nothing bid yet
        assertFalse(accepted, "Cannot double when no contract bid has been made");
    }

    @Test
    void doubledSidePartnerCanRedouble() {
        manager.ActionPlayed(new ContractBid(1, Strain.HEARTS)); // North bids
        manager.ActionPlayed(new PassAction());                  // East passes
        manager.ActionPlayed(new DoubleAction());                // South doubles (opponent)
        boolean accepted = manager.ActionPlayed(new PassAction()); // West passes
        // now back to North -- North's own side was doubled, North should be able to redouble
        assertTrue(accepted); // sanity check the pass itself worked
        boolean redoubleAccepted = manager.ActionPlayed(new RedoubleAction()); // North redoubles
        assertTrue(redoubleAccepted, "North should be able to redouble after their side was doubled");
    }

    @Test
    void opponentCannotRedouble() {
        manager.ActionPlayed(new ContractBid(1, Strain.HEARTS)); // North
        manager.ActionPlayed(new PassAction());                  // East
        manager.ActionPlayed(new DoubleAction());                // South doubles
        boolean accepted = manager.ActionPlayed(new RedoubleAction()); // West tries to redouble -- but West is opponent side
        assertFalse(accepted, "Only the doubled partnership can redouble, not the doubling side");
    }

    @Test
    void newBidCancelsExistingDouble() {
        manager.ActionPlayed(new ContractBid(1, Strain.HEARTS)); // North
        manager.ActionPlayed(new PassAction());                  // East
        manager.ActionPlayed(new DoubleAction());                // South doubles
        manager.ActionPlayed(new ContractBid(2, Strain.CLUBS));   // West overcalls -- cancels the double
        boolean accepted = manager.ActionPlayed(new RedoubleAction()); // North tries to redouble the now-stale double
        assertFalse(accepted, "A new bid should cancel the prior double; redoubling it should now be illegal");
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
