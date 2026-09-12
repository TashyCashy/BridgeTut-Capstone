package DoubleDummySolverSystem;

import logic.GameState;
import logic.PlayerPosition;

public class DoubleDummySolver {
    static int solve(GameState state){
        //given a gamestate do the double dummy analysis
        PlayerPosition declarerTeammate = state.getDeclarer().partner();
        return null;
    }
}
