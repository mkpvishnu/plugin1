package com.seasonsofconflict.models;

public enum Season {
    SPRING,
    SUMMER,
    FALL,
    WINTER;

    public Season getNext() {
        return values()[(ordinal() + 1) % values().length];
    }
}
