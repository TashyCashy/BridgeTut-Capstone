package BiddingSystem;

import BiddingSystem.BiddingData.Actions.ContractBid;
import BiddingSystem.BiddingData.Actions.DoubleAction;
import BiddingSystem.BiddingData.Actions.PassAction;
import BiddingSystem.BiddingData.Actions.RedoubleAction;
import BiddingSystem.BiddingData.DoublingState;
import BiddingSystem.BiddingLogic.BiddingManager;
import BiddingSystem.BiddingLogic.GameReset;
import logic.*;

import py4j.GatewayServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point exposed to the Python GUI via Py4J. Wraps BiddingManager so
 * Python never touches Java bidding classes directly — only primitives
 * (int, String, boolean) cross the language boundary.
 *
 * All translation (Strain <-> String, PlayerPosition <-> seat index) happens
 * here, in one place, rather than being duplicated on the Python side.
 */
public class BiddingGateway {

    private BiddingManager manager;
    private GameState gameState;

    public BiddingGateway() {
        this.manager = freshGame();
    }


    private BiddingManager freshGame() {
        Player south = new Player("South", new logic.PlayerHand(PlayerPosition.SOUTH), PlayerPosition.SOUTH);
        Player west  = new Player("West",  new logic.PlayerHand(PlayerPosition.WEST),  PlayerPosition.WEST);
        Player north = new Player("North", new logic.PlayerHand(PlayerPosition.NORTH), PlayerPosition.NORTH);
        Player east  = new Player("East",  new logic.PlayerHand(PlayerPosition.EAST),  PlayerPosition.EAST);
        // Order MUST match PlayerPosition.values(): SOUTH, WEST, NORTH, EAST
        Player[] players = { south, west, north, east };

        Deck deck = new Deck();
        deck.shuffle();
        DealCards.dealHands(deck, players);

        return new BiddingManager(PlayerPosition.SOUTH, players);
    }

    /**
     * Attempts a contract bid. Returns false on illegal bids OR bad input
     * (unknown strain name, out-of-range level) rather than throwing —
     * Python should treat any false as "show a rejection, don't update UI".
     */
    public boolean submitBid(int level, String strainName) {
        if (strainName == null){
            return false;
        }
        Strain strain;
        try {
            strain = Strain.valueOf(strainName);
        } catch (IllegalArgumentException e) {
            return false; // unknown strain name from Python side
        }
        if (level < 1 || level > 7) {
            return false;
        }
        return manager.ActionPlayed(new ContractBid(level, strain));
    }

    public boolean submitPass() {
        return manager.ActionPlayed(new PassAction());
    }

    public boolean submitDouble (){ return manager.ActionPlayed(new DoubleAction());}

    public boolean submitRedouble (){ return manager.ActionPlayed(new RedoubleAction());}

    /** Index into PlayerPosition.values() order: SOUTH=0, WEST=1, NORTH=2, EAST=3 */
    public int getCurrentSeatIndex() {
        return manager.getCurrentPlayer().getSeatPosition().ordinal();
    }

    public String getCurrentPlayerName() {
        return manager.getCurrentPlayer().getUsername();
    }

    public boolean checkBiddingOver() {
        return manager.checkBiddingOver();
    }

    public boolean isPassedOut() {
        return manager.isPassedOut();
    }

    /** Only meaningful after checkBiddingOver() returns true and isPassedOut() is false. */
    public String getDeclarerName() {
        return manager.getDeclarer().getUsername();
    }

    public int getDeclarerSeatIndex() {
        return manager.getDeclarer().getSeatPosition().ordinal();
    }

    /**
     * e.g. "2 HEARTS", or "2 HEARTS DOUBLED" / "2 HEARTS REDOUBLED" once the
     * auction has ended doubled. Python can format this however it wants.
     */
    public String getWinningContractString() {
        ContractBid c = manager.getWinningContract();
        String contract = c.getLevel() + " " + c.getStrain().name();
        if (manager.getFinalDoublingState() != DoublingState.UNDOUBLED) {
            contract += " " + manager.getFinalDoublingState().name();
        }
        return contract;
    }

    /**
     * Call when isPassedOut() is true. Replaces the internal manager with a
     * freshly dealt one and returns the new starting seat index so Python
     * can update whose turn it is.
     */
    public int resetAfterPassedOut() {
        this.manager = GameReset.resetGame(this.manager);
        return getCurrentSeatIndex();
    }

    //Live doubling state
    public String getCurrentDoublingState(){
        //py4j can only pass primitive types (String/int/boolean), so convert the enum to its name
        return manager.getCurrentDoublingState().name();
    }

    //Doubling state of final auction
    public String getFinalDoublingState(){
        return manager.getFinalDoublingState().name();
    }

    //for getting the rigth cards shown on the gui
    public List<String> getHandForSeat(int seatIndex) {
        Player p = manager.getPlayers()[seatIndex];
        List<String> cardCodes = new ArrayList<>();
        for (Card c : p.getPlayerHand().getHand()) {
            //adjusted for standard formula between backend and gui
            cardCodes.add(c.getSuit().getSuitLetter() + c.getRank().getRankLetter());
        }
        //for checking the cards are the same as their actual hand
        System.out.println("Hand for " +p.getUsername()+ " " + cardCodes);
        return cardCodes;
    }

    //TASHES CODE

    /**
     * Call once bidding has ended with a real contract (not passed out)
     * Pulls the declarer and trump suit straight from the manager that
     * just finished bidding - no re-entering data, no duplicated logic
     */
    public void startPlayPhase() {
        PlayerPosition declarer = manager.getDeclarer().getSeatPosition();
        Strain winningStrain = manager.getWinningContract().getStrain();
        Suit trumpSuit = winningStrain.toSuit();

        gameState = new GameState(declarer, trumpSuit);
        for (Player player: manager.getPlayers()) {
            gameState.dealHand(player.getSeatPosition(), player.getPlayerHand().getHand());
        }
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

    public String getCurrentDeclarerSeatIndex() {
        PlayerPosition currentDeclarer = manager.getCurrentDeclarer() ;
       if  (currentDeclarer == null) return "";
       else {
           return currentDeclarer.name();
       }
    }

    public static void main(String[] args) {
        GatewayServer server = new GatewayServer(new BiddingGateway());
        server.start();
        System.out.println("BiddingGateway started, listening for Python connections...");
    }
}
