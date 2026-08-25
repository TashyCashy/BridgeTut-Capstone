package logic;

/*
"Strain" in bridge means what a contract is played in. 
There are five possibilitites.
*/
public enum Strain {
    CLUBS, DIAMONDS, HEARTS, SPADES, NO_TRUMP;

    public Suit toSuit() {
        switch (this) {
            case CLUBS:
                return Suit.CLUBS;
            case DIAMONDS:
                return Suit.DIAMONDS;
            case HEARTS:
                return Suit.HEARTS;
            case SPADES:
                return Suit.SPADES;
            case NO_TRUMP:
                return null;
            default:
                throw new IllegalStateException();
        }
    }
    // for Vezi: 
    // Suit trumpSuit = winningBid.getStrain().toSuit();
    // automatically returns null if the contract was NO_TRUMP
}