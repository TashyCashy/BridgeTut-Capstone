package DoubleDummySolverSystem;

import logic.*;

import java.util.*;

public class DoubleDummySolver {
    //Transposition table to skip positions that we've already encountered, adds speed
    static Map<String, Integer> tpTable = new HashMap<>();

    static int solve(GameState state){
        int alpha = -1;
        int beta = 14;
        return solve(state, alpha, beta);
    }
    //overloaded method for alpha-beta pruning, used to optimise the solver method, not checking other branches if we've already found the best possible recursive sequence

    static int solve (GameState state, int alpha, int beta){
        //base case
        if (state.isHandComplete()) { return 0;}
        if (state.getCurrentTrick().getPlayedCards().isEmpty()){
           String stateKey = generateKey(state);
           if (tpTable.containsKey(stateKey)){
               return tpTable.get(stateKey);
               //we already have this game in our memery and thus know it plays out so skip computing it
           }
        }
        boolean cuttoff = false;
        //given a gamestate do the double dummy analysis
        PlayerPosition currentPlayer = state.getCurrentPlayerTurn();
        boolean MAX = playerIsMax(currentPlayer, state);

        PlayerHand playerHand = state.getHand(currentPlayer);
        List<Card> Cards = playerHand.getHand();
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
        legalCards = collapseEquivalentCards(legalCards, state); // skip cards that are provably interchangeable right now
        legalCards.sort(Card::compareRank);
        //reverse the sort order, so we try out better(higher value) cards, this is to try optimise alpha-beta pruning,
        // higher value pathways may eliminate more branches to check making the overall process faster, might make smaller trials slower however but valid tradeoff
        legalCards = legalCards.reversed();
        int bestValue = (MAX) ? 0 : 13;

        for (Card card : legalCards){
            GameState nextState = state.copy();
            int beforePlay = nextState.getCompletedTricks().size(); //track the trick size before playing
            nextState.playCard(currentPlayer, card); //play and shift mover
            int afterPlay = nextState.getCompletedTricks().size(); //use to compare if a trick was completed
            if (afterPlay > beforePlay){ //we have a winner for a trick
                //do we store the key here?
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
            //Add key to the hashmap?
            childValue = bonus + solve(nextState, alpha - bonus, beta - bonus);
            bestValue = (MAX) ? Math.max(bestValue,childValue) : Math.min(bestValue,childValue);

            if (MAX) { alpha = Math.max(alpha, bestValue); }
            else { beta = Math.min(beta, bestValue); }
            if (alpha >= beta) { cuttoff = true; break; }
        }
        if (state.getCurrentTrick().getPlayedCards().isEmpty() && !cuttoff){
            tpTable.put(generateKey(state), bestValue);
        }

        return bestValue;
    }
    //check if player is on the max side or the min side, true if max, false if min
    //Max is the team of the declarer, and min is the opposing team
    private static boolean playerIsMax (PlayerPosition player, GameState state ) {
        return player == state.getDeclarer() || player == state.getDeclarer().partner();
    }

    private static List<Rank> aliveRanksInSuit (GameState state, Suit suit){
        PlayerPosition [] positions = PlayerPosition.values();
        List<Rank>  aliveRanks = new ArrayList<>();
        for (PlayerPosition position : positions){
            PlayerHand currentHand = state.getHand(position);
            aliveRanks.addAll(currentHand.getRanksOfSuit(suit)) ;
        }
        Collections.sort(aliveRanks);
        return aliveRanks;
    }

    // reduces legalCards down to one representative per "run" of equivalent
    // cards, per suit, so solve() doesn't waste time trying cards that are
    // provably interchangeable
    private static List<Card> collapseEquivalentCards(List<Card> legalCards, GameState state) {
        // which suits are actually present among the legal cards
        Set<Suit> suitsPresent = new HashSet<>();
        for (Card card : legalCards) {
            suitsPresent.add(card.getSuit());
        }
        List<Card> collapsed = new ArrayList<>();
        for (Suit suit : suitsPresent) {
            List<Rank> alive = aliveRanksInSuit(state, suit);

            // which ranks of this suit are ours, among the legal cards
            Set<Rank> mine = new HashSet<>();
            for (Card card : legalCards) {
                if (card.getSuit() == suit) {
                    mine.add(card.getRank());
                }
            }

            for (int i = 0; i < alive.size(); i++) {
                Rank rank = alive.get(i);
                if (!mine.contains(rank)) continue; // not one of our cards, irrelevant here

                // top of a run there i nothing higher and still ours immediately above it
                boolean isTopOfRun = (i == alive.size() - 1) || !mine.contains(alive.get(i + 1));
                if (isTopOfRun) {
                    collapsed.add(findCard(legalCards, suit, rank));
                }
            }
        }
        return collapsed;
    }

    // finds the Card in legalCards matching this suit and rank - a hand never holds duplicates
    private static Card findCard(List<Card> cards, Suit suit, Rank rank) {
        for (Card card : cards) {
            if (card.getSuit() == suit && card.getRank() == rank) {
                return card;
            }
        }
        return null; // shouldn't happen coz mine was built from these same legalCards
    }

    private static String generateKey (GameState state){
        StringBuilder key = new StringBuilder();
        key.append(state.getCurrentPlayerTurn());
        key.append(" | ");
        for (PlayerPosition position : PlayerPosition.values()){
           List<Card> hand =  state.getHand(position).getHand();
           for (Card card : hand){
               key.append(card.toString());
               key.append(" | ");
           }
        }
        return key.toString();
    }
}
