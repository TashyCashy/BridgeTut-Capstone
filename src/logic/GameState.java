package logic;

import java.util.*;

public class GameState {
    private Map<PlayerPosition, PlayerHand> hands;
    private List<Trick> completedTricks;
    private Suit trumpSuit;
    private Trick currentTrick;
    private PlayerPosition currentPlayerTurn;
    private PlayerPosition declarer;

    public GameState(PlayerPosition declarer, Suit trumpSuit) {
        this.declarer = declarer;
        this.trumpSuit = trumpSuit;
        this.hands = new HashMap<>();
        this.completedTricks = new ArrayList<>();
        // do I need something for currentTrick and currentPlayerTurn
        this.currentTrick = Trick.startTrick(declarer);
        this.currentPlayerTurn = currentTrick.getLead(); // this is the player sitting to the left of the declarer
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

    public PlayerPosition getDeclarer() {
        return declarer;
    }

    public GameState copy(){
        GameState copy = new GameState(declarer, trumpSuit);
        copy.hands = new HashMap<>(hands);
        for (Map.Entry<PlayerPosition, PlayerHand> entry : hands.entrySet()) {
            copy.hands.put(entry.getKey(), entry.getValue().copy()); //since playerhand changes and is mutable, we want a seperate copy that the dds can experiment with and not change the one that is in the current game state
        }
        //make a copy for everything that is mutable, for the sake of the DDS
        copy.completedTricks = new ArrayList<>(this.completedTricks); //completedTricks are safe to have shared references
        copy.currentTrick = this.currentTrick.copy(); //still mutable
        copy.currentPlayerTurn = this.currentPlayerTurn; //enum safe share
        return copy;
    }
}
