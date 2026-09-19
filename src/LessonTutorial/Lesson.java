package LessonTutorial;

import BiddingSystem.BiddingData.Actions.PlayerAction;
import java.util.*;
import logic.*;

/**
 * Data model holding parsed lesson parameters, hands, auction scripts, card trick sequences, and pedagogical notes[cite: 25].
 */
public class Lesson {

    public String lessonId;
    public String title;
    public int mode;

    public PlayerPosition dealer;
    public String vulnerability;
    public List<PlayerAction> rawAuction = new ArrayList<>();
    public PlayerPosition declarer;
    public Suit trumpSuit;

    public String contractString;
    public Map<PlayerPosition, List<Card>> hands = new EnumMap<>(PlayerPosition.class);
    public List<List<Card>> tricks = new ArrayList<>();
    public LessonOutcome outcome;
    public String note;

    public Map<String, Map<PlayerPosition, Integer>> expectedTricksTable = new HashMap<>();

    /**
     * Constructs a Lesson object initializing empty hand maps for each seat[cite: 25].
     */
    public Lesson() {
        for (PlayerPosition pos : PlayerPosition.values()) {
            hands.put(pos, new ArrayList<>());
        }
    }

    /**
     * Checks if this lesson requires both bidding and play phases[cite: 25].
     *
     * @return {@code true} if dealer and rawAuction are present; {@code false} otherwise[cite: 25].
     */
    public boolean isBidAndPlayMode() {
        return dealer != null && rawAuction != null && !rawAuction.isEmpty();
    }

    /**
     * Determines the opening leader seat position (player to declarer's left)[cite: 25].
     *
     * @return Opening leader {@link PlayerPosition}, or {@code null} if declarer is unset[cite: 25].
     */
    public PlayerPosition getOpeningLeader() {
        if (declarer == null) return null;
        return PlayerPosition.values()[(declarer.ordinal() + 1) % 4];
    }

    /**
     * Gets the initial dealt hand for a given player position[cite: 25].
     *
     * @param position Seat position[cite: 25].
     * @return List of {@link Card} objects[cite: 25].
     */
    public List<Card> getHandForSeat(PlayerPosition position) {
        if (position == null || hands == null)
            return new ArrayList<>();
        return hands.getOrDefault(position, new ArrayList<>());
    }
}