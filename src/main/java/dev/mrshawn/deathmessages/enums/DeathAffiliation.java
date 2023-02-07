package dev.mrshawn.deathmessages.enums;


public enum DeathAffiliation {
    SOLO("Solo"),
    GANG("Gang");

    private final String value;

    DeathAffiliation(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
