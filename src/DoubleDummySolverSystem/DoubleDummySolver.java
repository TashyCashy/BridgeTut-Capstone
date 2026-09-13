package DoubleDummySolverSystem;

import logic.*;

import java.util.List;

public class DoubleDummySolver {
    static int solve(GameState state){
        //base case
        if (state.isHandComplete()) { return 0;}
        //given a gamestate do the double dummy analysis
        PlayerPosition currentPlayer = state.getCurrentPlayerTurn();
        boolean MAX = playerIsMax(currentPlayer, state);
        PlayerPosition mover = currentPlayer;
        //generate the mover's legal hands
        PlayerHand playerHand = state.getHand(currentPlayer);
        List<Card> Cards = state.getHand(currentPlayer).getHand();
        List<Card> legalCards;
        for (Card card : Cards){
            if (PlayValidation.isLegitPlay(playerHand))
        }


        return 8;
    }
    //check if player is on the max side or the min side, true if max, false if min
    //Max is the team of the declarer, and min is the opposing team
    private static boolean playerIsMax (PlayerPosition player, GameState state ) {
        return player == state.getDeclarer() || player == state.getDeclarer().partner();
    }
}
