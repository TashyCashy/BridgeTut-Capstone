package LessonTutorial;

import logic.*;
import java.util.*;

public class Lesson {

    public String lessonId;
    public String title;
    public int mode;

    public PlayerPosition dealer;
    public String vulnerability;
    public List<String> rawAuction = new ArrayList<>();
    public PlayerPosition declarer;
    public Suit trumpSuit;

    public String contractString;
    public Map<PlayerPosition, List<Card>> hands = new EnumMap<>(PlayerPosition.class);
    public List<List<Card>> tricks = new ArrayList<>();
    public LessonOutcome outcome;
    public String note;

    public Map<String, Map<PlayerPosition, Integer>> expectedTricksTable = new HashMap<>();

    public Lesson() {
        for (PlayerPosition pos : PlayerPosition.values()) {
            hands.put(pos, new ArrayList<>());
        }
    }

    public boolean isBidAndPlayMode() {
        return dealer != null && rawAuction != null && !rawAuction.isEmpty();
    }

    public PlayerPosition getOpeningLeader() {
        if (declarer == null) return null;
        return PlayerPosition.values()[(declarer.ordinal() + 1) % 4];
    }
}