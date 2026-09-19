package logic; 

import java.util.*;

/**
 * Maintains active play-phase state during a Bridge hand, including player hands,
 * completed tricks, turn tracking and trick winner determination.
 */
public class GameState {
    private Map<PlayerPosition, PlayerHand> hands;
    private List<Trick> completedTricks;
    private Suit trumpSuit;
    private Trick currentTrick;
    private PlayerPosition currentPlayerTurn;
    private PlayerPosition declarer;
    private List<PlayerPosition> trickWinner; // which team won each trick

    /**
     * Initialises a new play-phase session starting from the specifies declarer and trump suit.
     * 
     * @param declarer The {@link PlayerPosition} who won the bidding auction.
     * @param trumpSuit The winning contract's trump {@link Suit} (or {@code null}) for No-Trump.
     */
    public GameState(PlayerPosition declarer, Suit trumpSuit) {
        this.declarer = declarer;
        this.trumpSuit = trumpSuit;
        this.hands = new HashMap<>();
        this.completedTricks = new ArrayList<>();
        this.currentTrick = Trick.startTrick(declarer);
        this.currentPlayerTurn = currentTrick.getLead(); // this is the player sitting to the left of the declarer
        this.trickWinner = new ArrayList<>();
    }

    /**
     * Retrieves the hand assigned to a given seat position.
     * 
     * @param position The {@link PlayerPosition} seat to query.
     * @return The corresponding {@link PlayerHand}.
     */
    public PlayerHand getHand(PlayerPosition position) {
        return hands.get(position); // checking the already existing hand, not creating a new one each time
    }

    /**
     * Gets all tricks completed in the current hand so far.
     * 
     * @return Unmodifiable List of completed {@link Trick} objects.
     */
    public List<Trick> getCompletedTricks() {
        return this.completedTricks;
    }

    /**
     * Gets the active trick currently accepting card plays.
     * 
     * @return The current {@link Trick}.
     */
    public Trick getCurrentTrick() {
        return this.currentTrick;
    }

    /**
     * Gets the seat position of the player whose turn it is to play a card.
     * 
     * @return Active {@link PlayerPosition}.
     */
    public PlayerPosition getCurrentPlayerTurn() {
        return this.currentPlayerTurn;
    }

    /**
     * Gets the list of trick winning player seats in chronological order.
     * 
     * @return List of {@link PlayerPosition} winners.
     */
    public List<PlayerPosition> getTrickWinner() {
        return this.trickWinner;
    }

    /**
     * Assigns an initial hand of dealt cards to a specific seat.
     * 
     * @param position The {@link PlayerPosition} receiving the hand.
     * @param cards List of {@link Card} objects to deal.
     */
    public void dealHand(PlayerPosition position, List<Card> cards) {
        PlayerHand hand = new PlayerHand(position);
        for (Card card: cards) {
            hand.addCard(card);
        }
        hands.put(position, hand);
    }

    /**
     * Attempts to play a card from a player's hand into the active trick.
     * Validates turn order and suit-following rules before accepting.
     * 
     * @param player The {@link PlayerPosition} attempting the play.
     * @param card The {@link Card} being played.
     * @return {@code true} if the move was legal and accepted; {@code false} otherwise.
     */
    public boolean playCard(PlayerPosition player, Card card) {
        // check if it is this player's turn 
        if (player != currentPlayerTurn) 
            return false;
        
        PlayerHand hand = hands.get(player);
        if (!PlayValidation.isLegitPlay(hand, card, currentTrick))
            return false; // player did not follow the suit
        
        hand.removeCard(card);
        currentTrick.recordPlay(player, card);
        if (currentTrick.done())
            finishTrick();
        else 
            currentPlayerTurn = currentPlayerTurn.next();
        return true;
    }

    /**
     * Evaluates the completed trick winner, updates drick counts and sets up the next leader.
     */
    private void finishTrick() {
        PlayerPosition winner = PlayValidation.pickWinner(currentTrick, trumpSuit);
        completedTricks.add(currentTrick);
        trickWinner.add(winner); // forgot to populate trickwinner list
        currentTrick = new Trick(winner);
        currentPlayerTurn = winner;
    }

    /**
     * Checks if all 13 tricks in the hand have been played.
     * 
     * @return {@code true} if 13 tricks are complete; {@code false} otherwise.
     */
    public boolean isHandComplete() {
        return completedTricks.size() == 13; // 13 tricks per hand, not 4 tash
    }

    /**
     * Returns the partnership team name that won the most recent trick.
     * 
     * @return {@code "NORTH_SOUTH"}, {@code EAST_WEST} or {@code "NONE"} if no tricks completed yet.
     */
    public String getLatestTrickWinner() {
        if (trickWinner.isEmpty())
            return "NONE";
        PlayerPosition lastWinner = trickWinner.get(trickWinner.size()-1);
        if (lastWinner == PlayerPosition.NORTH || lastWinner == PlayerPosition.SOUTH)
            return "NORTH_SOUTH";
        else 
            return "EAST_WEST"; 
    }
}

