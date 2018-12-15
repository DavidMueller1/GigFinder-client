package com.example.david.gigfinder.data.enums;

public enum Genre {
    ALTERNATIVE,
    ELECTRONIC,
    EXPERMIENTAL,
    HIPHOP,
    TRAP,
    POP,
    RNB,
    LATINO,
    ROCK,
    PUNK,
    METAL,
    JAZZ,
    FOLK,
    TECHNO,
    HOUSE,
    SINGERSONGWRITER,;
    //TODO Add more

    @Override
    public String toString() {
        switch (this){
            case ALTERNATIVE:
                return "Alternative";
            case ELECTRONIC:
                return "Electronic";
            case EXPERMIENTAL:
                return "Experimental";
            case HIPHOP:
                return "Hip-Hop and Rap";
            case TRAP:
                return "Trap";
            case POP:
                return "Pop";
            case RNB:
                return "R&B";
            case LATINO:
                return "Latino";
            case ROCK:
                return "Rock";
            case PUNK:
                return "Punk";
            case METAL:
                return "Metal";
            case JAZZ:
                return "Jazz";
            case FOLK:
                return "Folk";
            case TECHNO:
                return "Techno";
            case HOUSE:
                return "House";
            case SINGERSONGWRITER:
                return "Singer Songwriter";
        }
        return "";
    }
}
