import logic.*;
import BiddingSystem.Player;
import BiddingSystem.DealCards;
import BiddingSystem.BiddingData.Actions.ContractBid;
import BiddingSystem.BiddingData.Actions.PassAction;
import BiddingSystem.BiddingData.Actions.PlayerAction;
import BiddingSystem.BiddingLogic.BiddingManager;

// Ported from origin/integration-test's root-level Driver.java and updated to the
// current BiddingManager API (getWinningStrain() was removed; the contract's
// strain now comes off getWinningContract() instead). Manual end-to-end smoke
// test, not a JUnit test: run it and eyeball that bidding hands off to the play
// engine correctly. Doesn't exercise double/redouble yet.
public class Driver {

    public static void main(String[] args) {
        // ---- set up players and deal cards ----
        // array order MUST match PlayerPosition's declared order (SOUTH, WEST, NORTH, EAST)
        // since BiddingManager indexes into this array using PlayerPosition.ordinal()
        Player south = new Player("South", new PlayerHand(PlayerPosition.SOUTH), PlayerPosition.SOUTH);
        Player west  = new Player("West",  new PlayerHand(PlayerPosition.WEST),  PlayerPosition.WEST);
        Player north = new Player("North", new PlayerHand(PlayerPosition.NORTH), PlayerPosition.NORTH);
        Player east  = new Player("East",  new PlayerHand(PlayerPosition.EAST),  PlayerPosition.EAST);
        Player[] players = { south, west, north, east };

        Deck deck = new Deck();
        deck.shuffle();
        DealCards.dealHands(deck, players);

        System.out.println("=== Hands dealt ===");
        for (Player p : players) {
            System.out.print(p.getUsername() + ": ");
            p.getPlayerHand().printHand();
            System.out.println();
        }

        // ---- run a scripted bidding sequence ----
        BiddingManager manager = new BiddingManager(PlayerPosition.SOUTH, players);

        System.out.println("\n=== Bidding ===");
        bid(manager, new PassAction());                  // South passes
        bid(manager, new ContractBid(1, Strain.CLUBS));   // West bids 1C
        bid(manager, new PassAction());                   // North passes
        bid(manager, new ContractBid(2, Strain.SPADES));  // East bids 2S
        bid(manager, new PassAction());                   // South passes
        bid(manager, new PassAction());                   // West passes
        bid(manager, new PassAction());                   // North passes - 3 in a row, bidding should end here

        boolean biddingOver = manager.checkBiddingOver();
        if (!biddingOver) {
            System.out.println("Bidding did not end as expected - check the sequence above");
            return;
        }
        if (manager.isPassedOut()) {
            System.out.println("Everyone passed - no contract this hand, nothing to play");
            return;
        }

        PlayerPosition declarerPosition = manager.getDeclarer().getSeatPosition();
        Suit trumpSuit = manager.getWinningContract().getStrain().toSuit(); // returns null automatically if NO_TRUMP

        System.out.println("\nDeclarer: " + declarerPosition + ", Trump suit: " + trumpSuit);

        // ---- hand off to the play engine ----
        GameState game = new GameState(declarerPosition, trumpSuit);
        for (Player p : players) {
            game.dealHand(p.getSeatPosition(), p.getPlayerHand().getHand());
        }

        System.out.println("\n=== Playing the hand ===");
        while (!game.isHandComplete()) {
            PlayerPosition current = game.getCurrentPlayerTurn();
            Card cardToPlay = pickLegalCard(game, current);
            boolean accepted = game.playCard(current, cardToPlay);
            System.out.println(current + " plays " + cardToPlay + " -> accepted? " + accepted);
        }

        System.out.println("\nHand complete? " + game.isHandComplete()); // expect true
        System.out.println("Total tricks played: " + game.getCompletedTricks().size()); // expect 13
    }

    // plays a bidding action and reports whether it was accepted
    private static void bid(BiddingManager manager, PlayerAction action) {
        Player actingPlayer = manager.getCurrentPlayer();
        boolean accepted = manager.ActionPlayed(action);
        System.out.println(actingPlayer.getUsername() + " attempts " + action.getDisplayString()
                + " -> " + (accepted ? "ACCEPTED" : "REJECTED"));
    }

    // walks a player's hand and returns the first card that's currently legal to play
    private static Card pickLegalCard(GameState game, PlayerPosition player) {
        PlayerHand hand = game.getHand(player);
        Trick trick = game.getCurrentTrick();
        for (Card card : hand.getHand()) {
            if (PlayValidation.isLegitPlay(hand, card, trick)) {
                return card;
            }
        }
        throw new IllegalStateException("No legal card found - shouldn't happen if isLegitPlay is correct");
    }
}
