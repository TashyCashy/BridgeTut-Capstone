package BiddingSystem;

import java.util.*;
import logic.*;

/**
 * Entry point exposed to the Python GUI via Py4J for managing the play phase of a live Bridge game[cite: 2, 21].
 * Wraps the underlying {@link GameState} so Python interacts strictly via primitives and string codes[cite: 21].
 */
public class PlayingGateway {
    private final GameState gameState;

    /**
     * Constructs a PlayingGateway wrapping the active game state[cite: 21].
     *
     * @param gameState The active {@link GameState} holding dealt hands and trick tracking[cite: 21].
     */
    public PlayingGateway(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Attempts to play a card on behalf of the specified player seat[cite: 21].
     *
     * @param seatIndex Integer index of the seat (0=SOUTH, 1=WEST, 2=NORTH, 3=EAST)[cite: 21].
     * @param cardCode  2-character card code string (e.g., "D6", "SA")[cite: 21].
     * @return {@code true} if the card play was legal and accepted; {@code false} otherwise[cite: 21].
     */
    public boolean playCard(int seatIndex, String cardCode) {
        PlayerPosition seat = PlayerPosition.values()[seatIndex];
        Card card = parseCardCode(cardCode);
        return gameState.playCard(seat, card);
    }

    /**
     * Checks if all 13 tricks in the hand have been played[cite: 21].
     *
     * @return {@code true} if the hand is complete; {@code false} otherwise[cite: 21].
     */
    public boolean isHandComplete() {
        return gameState.isHandComplete();
    }

    /**
     * Gets the total number of tricks completed in the current hand[cite: 21].
     *
     * @return Count of completed tricks[cite: 21].
     */
    public int getCompletedTricksCount() {
        return gameState.getCompletedTricks().size();
    }

    /**
     * Retrieves the list of remaining card codes held by a specified seat[cite: 21].
     *
     * @param seatIndex Integer index of the seat (0=SOUTH, 1=WEST, 2=NORTH, 3=EAST)[cite: 21].
     * @return List of card code strings remaining in hand (e.g., ["C6", "SA"])[cite: 21].
     */
    public List<String> getRemainingHandForSeat(int seatIndex) {
        PlayerPosition seat = PlayerPosition.values()[seatIndex];
        List<String> cardCodes = new ArrayList<>();
        for (Card card : gameState.getHand(seat).getHand()) {
            cardCodes.add(card.getSuit().getSuitLetter() + card.getRank().getRankLetter());
        }
        return cardCodes;
    } 

    /**
     * Gets the sequence of card codes played so far in the active trick[cite: 21].
     *
     * @return List of card code strings in order of play[cite: 21].
     */
    public List<String> getCurrentTrickCards() {
        List<String> cardCodes = new ArrayList<>();
        for (Card card : gameState.getCurrentTrick().getPlayedCards().values()) {
            cardCodes.add(card.getSuit().getSuitLetter() + card.getRank().getRankLetter());
        }
        return cardCodes;
    }

    /**
     * Gets the seat index of the player whose turn it is to play[cite: 21].
     *
     * @return Seat index (0=SOUTH, 1=WEST, 2=NORTH, 3=EAST)[cite: 21].
     */
    public int getCurrentTurnSeatIndex() {
        return gameState.getCurrentPlayerTurn().ordinal();
    }

    /**
     * Parses a 2-character card code string into a {@link Card} instance[cite: 21].
     *
     * @param code String code (e.g., "D6")[cite: 21].
     * @return Parsed {@link Card}[cite: 21].
     */
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
            default: return Rank.values()[Integer.parseInt(s) - 2];
        }
    }

    /**
     * Gets the seat index of the most recent trick winner[cite: 21].
     *
     * @return Winning seat index, or -1 if no tricks have finished yet[cite: 21].
     */
    public int getLatestTrickWinnerIdx() {
        List<PlayerPosition> winners = gameState.getTrickWinner();
        if (winners == null || winners.isEmpty())
            return -1;
        return winners.get(winners.size() - 1).ordinal();
    }

    /**
     * Gets the partnership team that won the latest trick[cite: 21].
     *
     * @return {@code "NORTH_SOUTH"}, {@code "EAST_WEST"}, or {@code "NONE"}[cite: 21].
     */
    public String getLatestTrickwinner() {
        return gameState.getLatestTrickWinner();
    }

    /**
     * Gets the number of tricks won by the North/South partnership[cite: 21].
     *
     * @return Trick count for North/South[cite: 21].
     */
    public int getNorthSouthTricks() {
        int count = 0;
        for (PlayerPosition winner : gameState.getTrickWinner()) {
            if (winner == PlayerPosition.NORTH || winner == PlayerPosition.SOUTH)
                count++;
        }
        return count;
    }

    /**
     * Gets the number of tricks won by the East/West partnership[cite: 21].
     *
     * @return Trick count for East/West[cite: 21].
     */
    public int getEastWestTricks() {
        int count = 0;
        for (PlayerPosition winner : gameState.getTrickWinner()) {
            if (winner == PlayerPosition.EAST || winner == PlayerPosition.WEST)
                count++;
        }
        return count;
    }
}