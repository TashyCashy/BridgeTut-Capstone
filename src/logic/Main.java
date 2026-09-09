package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // build a full 52-card deck manually and shuffle it
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);

        // set up the game: SOUTH is declarer, SPADES is trump
        PlayerPosition declarer = PlayerPosition.SOUTH;
        Suit trumpSuit = Suit.SPADES;
        GameState game = new GameState(declarer, trumpSuit);

        // deal 13 cards to each player, in seat order
        PlayerPosition[] seats = { PlayerPosition.SOUTH, PlayerPosition.WEST, PlayerPosition.NORTH, PlayerPosition.EAST };
        int cardIndex = 0;
        for (PlayerPosition seat : seats) {
            List<Card> hand = deck.subList(cardIndex, cardIndex + 13);
            game.dealHand(seat, new ArrayList<>(hand));
            cardIndex += 13;
        }

        System.out.println("=== Deal complete ===");
        for (PlayerPosition seat : seats) {
            System.out.println(seat + " hand size: " + game.getHand(seat).getHand().size()); // expect 13 each
        }
        System.out.println("Opening leader: " + game.getCurrentPlayerTurn()); // should be declarer.next()

        // sanity check: try an illegal move first (pick a card NOT in hand)
        System.out.println("\n=== Testing an illegal move ===");
        PlayerPosition firstPlayer = game.getCurrentPlayerTurn();
        Card notInHand = findCardNotInHand(game, firstPlayer);
        boolean illegalResult = game.playCard(firstPlayer, notInHand);
        System.out.println("Playing a card " + firstPlayer + " doesn't hold: " + notInHand
                + " -> accepted? " + illegalResult); // expect false

        // now play a full first trick legally
        System.out.println("\n=== Playing trick 1 ===");
        for (int i = 0; i < 4; i++) {
            PlayerPosition current = game.getCurrentPlayerTurn();
            Card cardToPlay = pickLegalCard(game, current);
            boolean result = game.playCard(current, cardToPlay);
            System.out.println(current + " plays " + cardToPlay + " -> accepted? " + result);
        }
        System.out.println("Tricks completed so far: " + game.getCompletedTricks().size()); // expect 1
        System.out.println("Next leader (trick winner): " + game.getCurrentPlayerTurn());

        // play out the rest of the hand automatically
        System.out.println("\n=== Playing remaining tricks ===");
        while (!game.isHandComplete()) {
            PlayerPosition current = game.getCurrentPlayerTurn();
            Card cardToPlay = pickLegalCard(game, current);
            game.playCard(current, cardToPlay);
        }
        System.out.println("Hand complete? " + game.isHandComplete()); // expect true
        System.out.println("Total tricks played: " + game.getCompletedTricks().size()); // expect 13

        // final sanity check: every hand should now be empty
        for (PlayerPosition seat : seats) {
            System.out.println(seat + " remaining cards: " + game.getHand(seat).getHand().size()); // expect 0 each
        }
    }

    // walks the player's hand and returns the first card that's a legal play right now
    private static Card pickLegalCard(GameState game, PlayerPosition player) {
        PlayerHand hand = game.getHand(player);
        Trick trick = game.getCurrentTrick();
        for (Card card : hand.getHand()) {
            if (PlayValidation.isLegitPlay(hand, card, trick)) {
                return card;
            }
        }
        throw new IllegalStateException("No legal card found - this shouldn't happen if isLegitPlay is correct");
    }

    // finds any card the given player does NOT currently hold, for testing illegal plays
    private static Card findCardNotInHand(GameState game, PlayerPosition player) {
        List<Card> theirHand = game.getHand(player).getHand();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                Card candidate = new Card(suit, rank);
                if (!theirHand.contains(candidate)) {
                    return candidate;
                }
            }
        }
        throw new IllegalStateException("Player somehow holds all 52 cards");
    }
}