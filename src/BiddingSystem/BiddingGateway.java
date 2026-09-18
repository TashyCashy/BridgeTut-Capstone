package BiddingSystem;

import BiddingSystem.BiddingData.Actions.ContractBid;
import BiddingSystem.BiddingData.Actions.DoubleAction;
import BiddingSystem.BiddingData.Actions.PassAction;
import BiddingSystem.BiddingData.Actions.RedoubleAction;
import BiddingSystem.BiddingData.DoublingState;
import BiddingSystem.BiddingLogic.BiddingManager;
import BiddingSystem.BiddingLogic.GameReset;
import BiddingSystem.Tutorial.TutorialGateway;
import logic.*;

import py4j.GatewayServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private TutorialGateway tutorialGateway;

    public BiddingGateway() {
        this.manager = freshGame();
        this.tutorialGateway = new TutorialGateway(); 
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
        //ranomizes the starting position of dealer
        return new BiddingManager(players[new Random().nextInt(4)].getSeatPosition(), players);
    }

    // expose TutorialGateway to python through py4j
    public TutorialGateway getTutorialGateway() {
        if (tutorialGateway == null)
            tutorialGateway = new TutorialGateway();
        return tutorialGateway;
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
     * Replaces the internal manager with a freshly dealt one and returns the
     * new starting seat index so Python can update whose turn it is. Called
     * both when isPassedOut() is true and when starting a brand new game
     * from the menu, so a leftover/abandoned game is never resumed.
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

    //Kept my implementation when merging, still works the same way
    public String getCurrentDeclarerSeatIndex() {
        PlayerPosition currentDeclarer = manager.getCurrentDeclarer();
        if (currentDeclarer == null) return "";
        else {
            return currentDeclarer.name();
        }
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

    //overwrote my startPlayPhase()
    /**
     * Call once bidding has ended with a real contract (not passed out)
     * Pulls the declarer and trump suit straight from the manager that
     * just finished bidding - no re-entering data, no duplicated logic
     */
    public PlayingGateway startPlayPhase() {
        PlayerPosition declarer = manager.getDeclarer().getSeatPosition();
        Strain winningStrain = manager.getWinningContract().getStrain();
        Suit trumpSuit = winningStrain.toSuit();

        gameState = new GameState(declarer, trumpSuit);
        for (Player player: manager.getPlayers()) {
            gameState.dealHand(player.getSeatPosition(), player.getPlayerHand().getHand());
        }
        return new PlayingGateway(gameState);
    }



    public boolean undoLastBid(){
        return manager.undoLastBid();
    }

    public static void main(String[] args) {
        GatewayServer server = new GatewayServer(new BiddingGateway());
        server.start();
        System.out.println("BiddingGateway started, listening for Python connections...");
    }
}
