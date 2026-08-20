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

    /**
     * 
     * @return player's partner
     */
    public PlayerPosition partner() {
        return next().next();
    }
}