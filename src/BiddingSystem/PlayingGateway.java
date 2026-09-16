package BiddingSystem;

import java.util.*;
import logic.*;

public class PlayingGateway {
    private final GameState gameState;

    public PlayingGateway(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     *Attempts to play a card on behalf of the given seat. 
     * cardCode matches getHandForSeat()'s format: suit letter then rank letter
     * @return true if the play was legal and applied, false if rejected
     */
    public boolean playCard(int seatIndex, String cardCode) {
        PlayerPosition seat = PlayerPosition.values()[seatIndex];
        Card card = parseCardCode(cardCode);
        return gameState.playCard(seat, card);
    }

    public boolean isHandComplete() {
        return gameState.isHandComplete();
    }

    public int getCompletedTricksCount() {
        return gameState.getCompletedTricks().size();
    }

    // Cards a seat currently still holds, same code format as getHandForSeat()
    public List<String> getRemainingHandForSeat(int seatIndex) {
        PlayerPosition seat = PlayerPosition.values()[seatIndex];
        List<String> cardCodes = new ArrayList<>();
        for (Card card : gameState.getHand(seat).getHand()) {
            cardCodes.add(card.getSuit().getSuitLetter() + card.getRank().getRankLetter());
        }
        return cardCodes;
    } 

    // cards played so far in the current trick, in the order they were played
    public List<String> getCurrentTrickCards() {
        List<String> cardCodes = new ArrayList<>();
        for (Card card: gameState.getCurrentTrick().getPlayedCards().values()) {
            cardCodes.add(card.getSuit().getSuitLetter() + card.getRank().getRankLetter());
        }
        return cardCodes;
    }

    // GUI needs to know whose turn it is
    public int getCurrentTurnSeatIndex() {
        return gameState.getCurrentPlayerTurn().ordinal();
    }

    // converts a code like "D6" back into a real card (suit letter, then rank)
    private Card parseCardCode(String code) {
        char suitChar = code.charAt(0);
        String rank = code.substring(1);
        return new Card(charToSuit(suitChar), codeToRank(rank));
    }

    private Suit charToSuit(char c) {
        switch (c) {
            case 'C': return Suit.CLUBS;
            case 'D': return Suit.DIAMONDS;
            case 'H': return Suit.HEARTS;
            case 'S': return Suit.SPADES;
            default: throw new IllegalArgumentException("Unknown suit code: " + c);
        }
    }

    private Rank codeToRank(String s) {
        switch (s) {
            case "J": return Rank.JACK;
            case "Q": return Rank.QUEEN;
            case "K": return Rank.KING;
            case "A": return Rank.ACE;
            default: return Rank.values()[Integer.parseInt(s)-2]; // "2"..."10"
        }
    }
}