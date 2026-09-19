package BiddingSystem.Tutorial;

import BiddingSystem.BiddingData.Actions.*;
import LessonTutorial.*;
import java.util.*;
import logic.*;

/**
 * Parser utility converting formatted text tutorial files into structured {@link Lesson} objects[cite: 22].
 * Supports single-lesson files and multi-lesson text archives[cite: 22].
 */
public class LessonParser {

    /**
     * Parses a raw text file containing multiple tutorial lesson blocks[cite: 22].
     *
     * @param rawText Full text content of the lesson file[cite: 22].
     * @return List of parsed {@link Lesson} objects[cite: 22].
     */
    public static List<Lesson> parseLessonFile(String rawText) {
        String[] lines = rawText.split("\\r?\\n");
        List<Integer> starts = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith("Cards held:")) {
                starts.add(i);
            }
        }

        List<Lesson> lessons = new ArrayList<>();
        for (int i = 0; i < starts.size(); i++) {
            int start = starts.get(i);
            int end = (i + 1 < starts.size()) ? starts.get(i + 1) : lines.length;
            String[] chunkLines = Arrays.copyOfRange(lines, start, end);
            String chunkText = String.join("\n", chunkLines);
            lessons.add(parseLessonText(chunkText));
        }
        return lessons;
    }

    /**
     * Parses a single lesson text block into a {@link Lesson} instance[cite: 22].
     *
     * @param rawText Raw text string representing one lesson[cite: 22].
     * @return Populated {@link Lesson} object[cite: 22].
     * @throws IllegalArgumentException If the header format is invalid[cite: 22].
     */
    public static Lesson parseLessonText(String rawText) {
        Lesson lesson = new Lesson();
        String[] lines = rawText.split("\\r?\\n");
        int lineIdx = 0;

        while (lineIdx < lines.length) {
            if (!lines[lineIdx].trim().isEmpty())
                break;
            lineIdx++;
        }

        if (lineIdx >= lines.length || !lines[lineIdx].trim().startsWith("Cards held:"))
            throw new IllegalArgumentException("Invalid lesson file header: Expected 'Cards held:'");
        lineIdx++;

        int handCount = 0;
        while (handCount < 4 && lineIdx < lines.length) {
            String line = lines[lineIdx].trim();
            lineIdx++;

            if (line.isEmpty())
                continue;

            parseHandLine(line, lesson);
            handCount++;
        }

        while (lineIdx < lines.length) {
            String line = lines[lineIdx].trim();
            lineIdx++;
            if (line.isEmpty())
                continue;

            if (line.startsWith("D:")) {
                String seat = line.substring(2).trim();
                lesson.dealer = parseSeat(seat);
            } else if (line.endsWith("VUL")) {
                lesson.vulnerability = line;
            } else if (line.startsWith("PLAY")) {
                String contractStr = line.substring(5).trim();
                lesson.contractString = contractStr;
                parseContractAndTrump(contractStr, lesson);
            } else if (line.contains(";") && !line.startsWith("NOTE:")) {
                String auction = line;
                if (auction.endsWith("."))
                    auction = auction.substring(0, auction.length() - 1);
                List<PlayerAction> auctionActions = new ArrayList<>();
                for (String token : auction.split(";")) {
                    auctionActions.add(parseBidToken(token));
                }
                lesson.rawAuction = auctionActions;
            } else if (line.equalsIgnoreCase("claim")) {
                lesson.outcome = LessonOutcome.CLAIM;
            } else if (line.equalsIgnoreCase("concede")) {
                lesson.outcome = LessonOutcome.CONCEDE;
            } else if (line.startsWith("NOTE:")) {
                lesson.note = line.substring(5).trim();
            } else if (line.contains(",")) {
                parseTrickLine(line, lesson);
            }
        }

        return lesson;
    }

    private static void parseHandLine(String line, Lesson lesson) {
        String[] parts = line.split(":", 2);
        if (parts.length < 2)
            return;

        String seatName = parts[0].trim();
        PlayerPosition seat = parseSeat(seatName);
        String suitData = parts[1].trim();

        String[] suits = suitData.split(";", -1);
        Suit[] suitOrder = {Suit.SPADES, Suit.HEARTS, Suit.DIAMONDS, Suit.CLUBS};
        List<Card> hand = lesson.hands.get(seat);

        for (int i = 0; i < suits.length && i < 4; i++) {
            String ranks = suits[i].trim();
            if (ranks.isEmpty() || ranks.equals("-"))
                continue;

            for (char rankChar : ranks.toCharArray()) {
                if (Character.isWhitespace(rankChar))
                    continue;
                Rank rank = parseRankChar(rankChar);
                if (rank != null) {
                    Card card = new Card(suitOrder[i], rank);
                    hand.add(card);
                }
            }
        }
    }

    private static void parseTrickLine(String line, Lesson lesson) {
        String[] cardCodes = line.split(",");
        List<Card> trick = new ArrayList<>();

        for (String code : cardCodes) {
            code = code.trim();
            if (code.length() < 2)
                continue;
            char rankChar = code.charAt(0);
            char suitChar = code.charAt(1);
            Rank rank = parseRankChar(rankChar);
            Suit suit = parseSuitChar(suitChar);

            if (rank != null && suit != null) {
                Card card = new Card(suit, rank);
                trick.add(card);
            }
        }

        if (!trick.isEmpty())
            lesson.tricks.add(trick);
    }

    private static PlayerPosition parseSeat(String seat) {
        seat = seat.toUpperCase();
        switch (seat) {
            case "N": return PlayerPosition.NORTH;
            case "E": return PlayerPosition.EAST;
            case "S": return PlayerPosition.SOUTH;
            case "W": return PlayerPosition.WEST;
            default: return PlayerPosition.SOUTH;
        }
    }

    private static Rank parseRankChar(char c) {
        c = Character.toUpperCase(c);
        switch (c) {
            case 'A': return Rank.ACE;
            case 'K': return Rank.KING;
            case 'Q': return Rank.QUEEN;
            case 'J': return Rank.JACK;
            case 'T': return Rank.TEN;
            case '9': return Rank.NINE;
            case '8': return Rank.EIGHT;
            case '7': return Rank.SEVEN;
            case '6': return Rank.SIX;
            case '5': return Rank.FIVE;
            case '4': return Rank.FOUR;
            case '3': return Rank.THREE;
            case '2': return Rank.TWO;
            default: return null;
        }
    }

    private static Suit parseSuitChar(char c) {
        c = Character.toUpperCase(c);
        switch (c) {
            case 'C': return Suit.CLUBS;
            case 'D': return Suit.DIAMONDS;
            case 'H': return Suit.HEARTS;
            case 'S': return Suit.SPADES;
            default: return null;
        }
    }

    private static void parseContractAndTrump(String contractStr, Lesson lesson) {
        if (contractStr.endsWith("NT") || contractStr.endsWith("N")) {
            lesson.trumpSuit = null;
        } else if (contractStr.length() >= 2) {
            char suitChar = contractStr.charAt(contractStr.length() - 1);
            lesson.trumpSuit = parseSuitChar(suitChar);
        }

        if (lesson.declarer == null) {
            lesson.declarer = PlayerPosition.SOUTH;
        }
    }

    private static PlayerAction parseBidToken(String token) {
        token = token.trim().toUpperCase();
        if (token.equals("P")) {
            return new PassAction();
        } else if (token.equals("DBL")) {
            return new DoubleAction();
        } else if (token.equals("RDBL")) {
            return new RedoubleAction();
        } else {
            Strain strain = null;
            int level = Character.getNumericValue(token.charAt(0));
            if (token.endsWith("NT") || token.endsWith("N")) {
                strain = Strain.NO_TRUMP;
            } else {
                strain = parseSuitChar(token.charAt(1)).toStrain();
            }
            return new ContractBid(level, strain);
        }
    }
}