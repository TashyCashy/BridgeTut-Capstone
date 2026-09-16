package logic; 

import java.util.*;

public class GameState {
    private Map<PlayerPosition, PlayerHand> hands;
    private List<Trick> completedTricks;
    private Suit trumpSuit;
    private Trick currentTrick;
    private PlayerPosition currentPlayerTurn;
    private PlayerPosition declarer;
    private List<PlayerPosition> trickWinner; // which team won each trick

    public GameState(PlayerPosition declarer, Suit trumpSuit) {
        this.declarer = declarer;
        this.trumpSuit = trumpSuit;
        this.hands = new HashMap<>();
        this.completedTricks = new ArrayList<>();
        this.currentTrick = Trick.startTrick(declarer);
        this.currentPlayerTurn = currentTrick.getLead(); // this is the player sitting to the left of the declarer
        this.trickWinner = new ArrayList<>();
    }

    public PlayerHand getHand(PlayerPosition position) {
        return hands.get(position); // checking the already existing hand, not creating a new one each time
    }

    public List<Trick> getCompletedTricks() {
        return this.completedTricks;
    }

    public Trick getCurrentTrick() {
        return this.currentTrick;
    }

    public PlayerPosition getCurrentPlayerTurn() {
        return this.currentPlayerTurn;
    }

    public void dealHand(PlayerPosition position, List<Card> cards) {
        PlayerHand hand = new PlayerHand(position);
        for (Card card: cards) {
            hand.addCard(card);
        }
        hands.put(position, hand);
    }

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

    private void finishTrick() {
        PlayerPosition winner = PlayValidation.pickWinner(currentTrick, trumpSuit);
        completedTricks.add(currentTrick);
        currentTrick = new Trick(winner);
        currentPlayerTurn = winner;
    }

    public boolean isHandComplete() {
        return completedTricks.size() == 13; // 13 tricks per hand, not 4 tash
    }

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

