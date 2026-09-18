package BiddingSystem.Tutorial;

import BiddingSystem.BiddingData.Actions.*;
import LessonTutorial.*;
import java.util.*;
import logic.*;

public class LessonParser {
    public static List<Lesson> parseLessonFile (String rawText){
        String [] lines = rawText.split("\\r?\\n");
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
    // reads the lesson text and creates a lesson object
    public static Lesson parseLessonText(String rawText) {
        Lesson lesson = new Lesson();
        String[] lines = rawText.split("\\r?\\n");
        int lineIdx = 0;

        // skip any empty lines in the beginning
        while (lineIdx < lines.length) {
            if (!lines[lineIdx].trim().isEmpty())
                break;
            lineIdx++;
        }

        // checking for correct start to text
        if (lineIdx >= lines.length || !lines[lineIdx].trim().startsWith("Cards held:"))
            throw new IllegalArgumentException("Invalid lesson file header: Expected 'Cards held:'");
        lineIdx++;

        // read the players' hands
        int handCount = 0;
        while (handCount < 4 && lineIdx < lines.length) {
            String line = lines[lineIdx].trim();
            lineIdx++;

            if (line.isEmpty()) // ignoring empty lines
                continue;

            parseHandLine(line, lesson);
            handCount++;
        }

        // reading the rest of the lesson
        while (lineIdx < lines.length) {
            String line = lines[lineIdx].trim();
            lineIdx++;
            if (line.isEmpty()) // ignoring empty lines
                continue;

            // reading the dealer
            if (line.startsWith("D:")) {
                String seat = line.substring(2).trim();
                lesson.dealer = parseSeat(seat);
            }

            // reading vulnerability
            else if (line.endsWith("VUL"))
                lesson.vulnerability = line;

            // reading the contract for a play-only lesson
            else if (line.startsWith("PLAY")) {
                String contractStr = line.substring(5).trim();
                lesson.contractString = contractStr;
                parseContractAndTrump(contractStr, lesson); // parse trumpSuit and default delcarer for mode 2 play lessons
            }

            // reading the auction
            else if (line.contains(";") && !line.startsWith("NOTE:")) {
                String auction = line;
                if (auction.endsWith("."))
                    auction = auction.substring(0, auction.length()-1);
                List<PlayerAction> auctionActions = new ArrayList<>();
                for (String token : auction.split(";")) {
                    auctionActions.add(parseBidToken(token));
                }
                lesson.rawAuction = auctionActions;
            }

            // reading the outcome
            else if (line.equalsIgnoreCase("claim"))
                lesson.outcome = LessonOutcome.CLAIM;

            else if (line.equalsIgnoreCase("concede"))
                lesson.outcome = LessonOutcome.CONCEDE;

            // reading the lesson note
            else if (line.startsWith("NOTE:"))
                lesson.note = line.substring(5).trim();

            // reading a trick
            else if (line.contains(","))
                parseTrickLine(line, lesson);
        }

        return lesson;
    }

    // read a player's hand
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

        // going through each suit
        for (int i = 0; i < suits.length && i < 4; i++) {
            String ranks = suits[i].trim();
            if (ranks.isEmpty() || ranks.equals("-")) // player doesn't have cards in this suit
                continue;

            // go through each card in said suit
            for (char rankChar: ranks.toCharArray()) {
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

    // reads the four cards in a trick
    private static void parseTrickLine(String line, Lesson lesson) {
        String[] cardCodes = line.split(",");
        List<Card> trick = new ArrayList<>();

        for (String code: cardCodes) {
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

    // convert N, S, W, E to player positions
    private static PlayerPosition parseSeat(String seat) {
        seat = seat.toUpperCase();
        switch (seat) {
            case "N":
                return PlayerPosition.NORTH;
            case "E":
                return PlayerPosition.EAST;
            case "S":
                return PlayerPosition.SOUTH;
            case "W":
                return PlayerPosition.WEST;
            default:
                return PlayerPosition.SOUTH;
        }
    }

    // converts card character into rank
    private static Rank parseRankChar(char c) {
        c = Character.toUpperCase(c);
        switch (c) {
            case 'A':
                return Rank.ACE;
            case 'K':
                return Rank.KING;
            case 'Q':
                return Rank.QUEEN;
            case 'J':
                return Rank.JACK;
            case 'T':
                return Rank.TEN;
            case '9':
                return Rank.NINE;
            case '8':
                return Rank.EIGHT;
            case '7':
                return Rank.SEVEN;
            case '6':
                return Rank.SIX;
            case '5':
                return Rank.FIVE;
            case '4':
                return Rank.FOUR;
            case '3':
                return Rank.THREE;
            case '2':
                return Rank.TWO;
            default:
                return null;
        }
    }

    // converts character to suit
    private static Suit parseSuitChar(char c) {
        c = Character.toUpperCase(c);

        switch (c) {
            case 'C':
                return Suit.CLUBS;
            case 'D':
                return Suit.DIAMONDS;
            case 'H':
                return Suit.HEARTS;
            case 'S':
                return Suit.SPADES;
            default:
                return null;
        }
    }

    private static void parseContractAndTrump(String contractStr, Lesson lesson) {
    // Standard Mode 2 contracts look like "4S", "3NT", "2H"
    if (contractStr.endsWith("NT") || contractStr.endsWith("N")) {
        lesson.trumpSuit = null; // No Trump
    } else if (contractStr.length() >= 2) {
        char suitChar = contractStr.charAt(contractStr.length() - 1);
        lesson.trumpSuit = parseSuitChar(suitChar);
    }

    // For Mode 2 play-only lessons, Declarer is SOUTH by standard convention
    if (lesson.declarer == null) {
        lesson.declarer = PlayerPosition.SOUTH;
    }
}

private static PlayerAction parseBidToken (String token) {
        token = token.trim().toUpperCase();
    if (token.equals("P")) {
        return new PassAction();
    }
    else if (token.equals("DBL")) {
        return new DoubleAction();
    }
    else if (token.equals("RDBL")) {
        return new RedoubleAction();
    }
    else{
        Strain strain = null;
        //it's a contract bid so we need to extract rank, suit, and
        //first value is the rank of the card
        int level = Character.getNumericValue(token.charAt(0));
        //the rest of it is the strain, woulf us
        if (token.endsWith("NT") || token.endsWith("N")) {
            strain = Strain.NO_TRUMP;
        }
        else {
            //tash's function already does this for us :)
            strain = parseSuitChar(token.charAt(1)).toStrain();
        }
        return new ContractBid(level, strain);
    }
}
}
