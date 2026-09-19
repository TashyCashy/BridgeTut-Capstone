package logic;

/**
 * Enumerates the four seat positions around the Bridge table.
 */
public enum PlayerPosition {
    SOUTH, WEST, NORTH, EAST;

    /**
     * Determines the seat position of the player seated to this player's left (clockwise rotation).
     *
     * @return Next {@link PlayerPosition}.
     */
    public PlayerPosition next() {
        PlayerPosition[] positions = values();
        int nxtIdx = (this.ordinal() + 1) % positions.length;
        return positions[nxtIdx];
    }

    /**
     * Determines the seat position of the player seated to this player's right (counter-clockwise rotation).
     *
     * @return Previous {@link PlayerPosition}.
     */
    public PlayerPosition previous() {
        PlayerPosition[] positions = values();
        int prevIdx = (this.ordinal() - 1 + positions.length) % positions.length;
        return positions[prevIdx];
    }

    /**
     * Determines the seat position of this player's partner (opposite seat).
     *
     * @return Partner's {@link PlayerPosition}.
     */
    public PlayerPosition partner() {
        return next().next();
    }
}