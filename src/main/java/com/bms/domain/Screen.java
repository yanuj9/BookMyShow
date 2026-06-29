package com.bms.domain;

import lombok.Getter;
import java.util.List;

@Getter
public class Screen {
    private final String id;
    private final String name;
    private final List<Seat> seats; // Layout of physical seats in this screen

    public Screen(String id, String name, List<Seat> seats) {
        this.id = id;
        this.name = name;
        this.seats = seats;
    }
}
