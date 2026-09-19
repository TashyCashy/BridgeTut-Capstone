package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command-line test harness demonstrating full-hand generation, dealing, turn validation,
 * and automated trick simulation for the Bridge engine.
 */
public class Main {

    /**
     * Execution entry point for running engine simulation tests.
     *
     * @param args Command-line execution arguments.
     */
    public static void main(String[] args) {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);

        PlayerPosition declarer = PlayerPosition.SOUTH;
        Suit trumpSuit = Suit.SPADES;
        GameState game = new GameState(declarer, trumpSuit);

        PlayerPosition[] seats = { PlayerPosition.SOUTH, PlayerPosition.WEST, PlayerPosition.NORTH, PlayerPosition.EAST };
        int cardIndex = 0;
        for (PlayerPosition seat : seats) {
            List<Card> hand = deck.subList(cardIndex, cardIndex + 13);
            game.dealHand(seat, new ArrayList<>(hand));
            cardIndex += 13;
        }

        System.out.println("=== Deal complete ===");
        for (PlayerPosition seat : seats) {
            System.out.println(seat + " hand size: " + game.getHand(seat).getHand().size());
        }
        System.out.println("Opening leader: " + game.getCurrentPlayerTurn());

        System.out.println("\n=== Testing an illegal move ===");
        PlayerPosition firstPlayer = game.getCurrentPlayerTurn();
        Card notInHand = findCardNotInHand(game, firstPlayer);
        boolean illegalResult = game.playCard(firstPlayer, notInHand);
        System.out.println("Playing a card " + firstPlayer + " doesn't hold: " + notInHand
                + " -> accepted? " + illegalResult);

        System.out.println("\n=== Playing trick 1 ===");
        for (int i = 0; i < 4; i++) {
            PlayerPosition current = game.getCurrentPlayerTurn();
            Card cardToPlay = pickLegalCard(game, current);
            boolean result = game.playCard(current, cardToPlay);
            System.out.println(current + " plays " + cardToPlay + " -> accepted? " + result);
        }
        System.out.println("Tricks completed so far: " + game.getCompletedTricks().size());
        System.out.println("Next leader (trick winner): " + game.getCurrentPlayerTurn());

        System.out.println("\n=== Playing remaining tricks ===");
        while (!game.isHandComplete()) {
            PlayerPosition current = game.getCurrentPlayerTurn();
            Card cardToPlay = pickLegalCard(game, current);
            game.playCard(current, cardToPlay);
        }
        System.out.println("Hand complete? " + game.isHandComplete());
        System.out.println("Total tricks played: " + game.getCompletedTricks().size());

        for (PlayerPosition seat : seats) {
            System.out.println(seat + " remaining cards: " + game.getHand(seat).getHand().size());
        }
    }

    /**
     * Walks a player's hand to find the first legal card playable for the active trick.
     */
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

    /**
     * Finds a card that a given player does not hold for testing illegal plays.
     */
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