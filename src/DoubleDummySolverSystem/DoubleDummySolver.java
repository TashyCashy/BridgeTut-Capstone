package DoubleDummySolverSystem;

import logic.*;

import java.util.ArrayList;
import java.util.List;

public class DoubleDummySolver {
    static int solve(GameState state){
        //base case
        if (state.isHandComplete()) { return 0;}
        //given a gamestate do the double dummy analysis
        PlayerPosition currentPlayer = state.getCurrentPlayerTurn();
        boolean MAX = playerIsMax(currentPlayer, state);
        PlayerPosition mover = currentPlayer;

        PlayerHand playerHand = state.getHand(currentPlayer);
        List<Card> Cards = state.getHand(currentPlayer).getHand();
        //generate the mover's legal hands
        List<Card> legalCards = new ArrayList<>();
        //added recusrively
        int bonus = 0;
        int childValue = 0;
        for (Card card : Cards){
            if (PlayValidation.isLegitPlay(playerHand, card, state.getCurrentTrick())){
                legalCards.add(card);
            }
        }
        for (Card card : legalCards){
            GameState nextState = state.copy();
            int beforePlay = nextState.getCompletedTricks().size(); //track the trick size before playing
            nextState.playCard(currentPlayer, card); //play and shift mover
            int afterPlay = nextState.getCompletedTricks().size(); //use to compare if a trick was completed
            if (afterPlay > beforePlay){ //we have a winner for a trick
                if (playerIsMax(nextState.getCurrentPlayerTurn(), nextState)) { //finishTrick ends on getCurrentPlayerTurn meaning that is the winner
                    bonus = 1;
                }
                else { //min side won
                bonus = 0;
                }
            }
            else{
                bonus = 0;// bonus stays zero if trick is not over
            }
            //value of child node, kicks off recursion
            childValue = bonus + solve(nextState);
        }
        return childValue;
    }
    //check if player is on the max side or the min side, true if max, false if min
    //Max is the team of the declarer, and min is the opposing team
    private static boolean playerIsMax (PlayerPosition player, GameState state ) {
        return player == state.getDeclarer() || player == state.getDeclarer().partner();
    }
}
