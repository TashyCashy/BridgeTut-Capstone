package DoubleDummySolverSystem;

import logic.*;
import BiddingSystem.DealCards;
import BiddingSystem.Player;

import java.util.List;

/**
 * Manual trial harness for DoubleDummySolver - NOT a JUnit test, just a
 * runnable console check. Builds small, hand-constructed GameStates (a
 * couple of cards per seat, single suit, No Trump) and prints solve()'s
 * answer alongside the exact hands dealt, so the correct trick count can be
 * worked out on paper and compared against what the solver actually returns.
 */
public class SolverTrial {

    public static void main(String[] args) {
       /* System.out.println("=== Trial 1: one card each ===");
        runTrial(oneCardEach());

        System.out.println();
        System.out.println("=== Trial 2: two cards each (real minimax exercise) ===");
        runTrial(twoCardsEach());

        System.out.println();
        System.out.println("=== Trial 3: four cards each, two suits ===");
        runTrial(fourCardsEach());*/

        /*System.out.println();
        System.out.println("=== Trial 4: full 52-card shuffled deck ===");
        GameState fullDeck = fullShuffledDeck();
        runTrial(fullDeck);*/

        /*System.out.println("=== Trial 5: 7 cards each, real shuffled deal ===");
        runTrial(sevenCardsEachShuffled());*/

        System.out.println("=== Trial 6: 12 cards each, real shuffled deal ===");
        runTrial(twelveCardsEachShuffled());
    }

    private static void runTrial(GameState state) {
        printSetup(state);
        long start = System.nanoTime();
        int result = DoubleDummySolver.solve(state);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("solve() says declaring side takes: " + result);
        System.out.println("time taken: " + elapsedMs + "ms");
    }

    private static void printSetup(GameState state) {
        System.out.println("Declarer: " + state.getDeclarer()
                + " (declaring side: " + state.getDeclarer() + " + " + state.getDeclarer().partner() + ")");
        System.out.println("On lead: " + state.getCurrentPlayerTurn());
        for (PlayerPosition pos : PlayerPosition.values()) {
            System.out.print(pos + ": ");
            for (Card c : state.getHand(pos).getHand()) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    // 1 trick left, everyone has exactly 1 card - no decisions possible for
    // anyone, so this only confirms the plumbing works and the right seat's
    // card is recognised as the winner.
    private static GameState oneCardEach() {
        GameState state = new GameState(PlayerPosition.SOUTH, null); // No Trump
        state.dealHand(PlayerPosition.WEST,  List.of(new Card(Suit.HEARTS, Rank.ACE)));
        state.dealHand(PlayerPosition.NORTH, List.of(new Card(Suit.HEARTS, Rank.KING)));
        state.dealHand(PlayerPosition.EAST,  List.of(new Card(Suit.HEARTS, Rank.QUEEN)));
        state.dealHand(PlayerPosition.SOUTH, List.of(new Card(Suit.HEARTS, Rank.JACK)));
        return state;
    }

    // 2 tricks left, everyone has 2 cards, single suit (no follow-suit
    // complications to worry about) - each of the 4 honours (A,K,Q,J) and
    // each of the 4 spot cards (2,3,4,5) is split one per hand. Genuine
    // choices exist at more than one point - work this one out on paper.
    private static GameState twoCardsEach() {
        GameState state = new GameState(PlayerPosition.SOUTH, null); // No Trump
        state.dealHand(PlayerPosition.WEST,  List.of(new Card(Suit.HEARTS, Rank.ACE),   new Card(Suit.HEARTS, Rank.TWO)));
        state.dealHand(PlayerPosition.NORTH, List.of(new Card(Suit.HEARTS, Rank.KING),  new Card(Suit.HEARTS, Rank.THREE)));
        state.dealHand(PlayerPosition.EAST,  List.of(new Card(Suit.HEARTS, Rank.QUEEN), new Card(Suit.HEARTS, Rank.FOUR)));
        state.dealHand(PlayerPosition.SOUTH, List.of(new Card(Suit.HEARTS, Rank.JACK),  new Card(Suit.HEARTS, Rank.FIVE)));
        return state;
    }

    // 4 tricks left, everyone has 4 cards across two suits (hearts + spades,
    // 2 of each) - same honour/spot-card split as trial 2, doubled into a
    // second suit. Nobody starts void in either suit, but now there's a real
    // choice of which suit to lead from, not just which card within a suit -
    // and it's worth checking on paper whether a void ever actually opens up.
    private static GameState fourCardsEach() {
        GameState state = new GameState(PlayerPosition.SOUTH, null); // No Trump
        state.dealHand(PlayerPosition.WEST, List.of(
                new Card(Suit.HEARTS, Rank.ACE),   new Card(Suit.HEARTS, Rank.TWO),
                new Card(Suit.SPADES, Rank.ACE),   new Card(Suit.SPADES, Rank.TWO)));
        state.dealHand(PlayerPosition.NORTH, List.of(
                new Card(Suit.HEARTS, Rank.KING),  new Card(Suit.HEARTS, Rank.THREE),
                new Card(Suit.SPADES, Rank.KING),  new Card(Suit.SPADES, Rank.THREE)));
        state.dealHand(PlayerPosition.EAST, List.of(
                new Card(Suit.HEARTS, Rank.QUEEN), new Card(Suit.HEARTS, Rank.FOUR),
                new Card(Suit.SPADES, Rank.QUEEN), new Card(Suit.SPADES, Rank.FOUR)));
        state.dealHand(PlayerPosition.SOUTH, List.of(
                new Card(Suit.HEARTS, Rank.JACK),  new Card(Suit.HEARTS, Rank.FIVE),
                new Card(Suit.SPADES, Rank.JACK),  new Card(Suit.SPADES, Rank.FIVE)));
        return state;
    }

    // A real, fully shuffled 52-card deal, all 13 tricks - the same
    // deal-then-hand-off pattern as Driver.java and CopyTest, feeding one
    // real DealCards.dealHands() call into GameState. solve() is NOT called
    // on this in main() - unpruned minimax over a full deal is a known
    // intractable search (this is exactly why real double-dummy solvers need
    // alpha-beta and equivalent-card pruning, not just "write minimax").
    // Calling it here is expected to run for a very long time with nothing
    // to show for it, regardless of whether the max/min bug above is fixed -
    // the bug doesn't change how much work is done, only which answer comes
    // back. Revisit once alpha-beta + pruning exist.
    private static GameState fullShuffledDeck() {
        Deck deck = new Deck();
        deck.shuffle();
        Player south = new Player("South", new PlayerHand(PlayerPosition.SOUTH), PlayerPosition.SOUTH);
        Player west  = new Player("West",  new PlayerHand(PlayerPosition.WEST),  PlayerPosition.WEST);
        Player north = new Player("North", new PlayerHand(PlayerPosition.NORTH), PlayerPosition.NORTH);
        Player east  = new Player("East",  new PlayerHand(PlayerPosition.EAST),  PlayerPosition.EAST);
        Player[] players = { south, west, north, east };
        DealCards.dealHands(deck, players);

        GameState state = new GameState(PlayerPosition.SOUTH, Suit.SPADES); // a real trump this time, not NT
        for (Player p : players) {
            state.dealHand(p.getSeatPosition(), p.getPlayerHand().getHand());
        }
        return state;
    }

    // A real shuffled deck, but only 7 cards dealt to each seat (28 of the 52
    // cards used) - a genuine, messy distribution rather than a constructed
    // best case, but small enough to actually measure safely in this sandbox.
    private static GameState sevenCardsEachShuffled() {
        Deck deck = new Deck();
        deck.shuffle();
        PlayerPosition[] seats = { PlayerPosition.SOUTH, PlayerPosition.WEST, PlayerPosition.NORTH, PlayerPosition.EAST };
        GameState state = new GameState(PlayerPosition.SOUTH, Suit.SPADES);
        for (PlayerPosition seat : seats) {
            List<Card> hand = new java.util.ArrayList<>();
            for (int i = 0; i < 7; i++) {
                hand.add(deck.drawCard());
            }
            state.dealHand(seat, hand);
        }
        return state;
    }

    // A real shuffled deck, 12 cards each (48 of the 52 cards used) - one
    // size up from the 7-card trial, to see how far the transposition table
    // + move ordering + alpha-beta actually push the solver now.
    private static GameState twelveCardsEachShuffled() {
        Deck deck = new Deck();
        deck.shuffle();
        PlayerPosition[] seats = { PlayerPosition.SOUTH, PlayerPosition.WEST, PlayerPosition.NORTH, PlayerPosition.EAST };
        GameState state = new GameState(PlayerPosition.SOUTH, Suit.SPADES);
        for (PlayerPosition seat : seats) {
            List<Card> hand = new java.util.ArrayList<>();
            for (int i = 0; i < 12; i++) {
                hand.add(deck.drawCard());
            }
            state.dealHand(seat, hand);
        }
        return state;
    }
}
