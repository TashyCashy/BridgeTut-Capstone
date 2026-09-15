package logic;

public enum PlayerPosition {
    SOUTH, WEST, NORTH, EAST;

    /**
     *
     * @return player seated to this specific player's left
     */
    public PlayerPosition next() {
        PlayerPosition[] positions = values();
        int nxtIdx = (this.ordinal() + 1)%positions.length;
        return positions[nxtIdx];
    }
    //for undo button
    public PlayerPosition previous() {
        PlayerPosition[] positions = values();
        int prevIdx = (this.ordinal() - 1 + positions.length)%positions.length;
        return positions[prevIdx];
    }

    /**
     *
     * @return player's partner
     */
    public PlayerPosition partner() {
        return next().next();
    }
}
