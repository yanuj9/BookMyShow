package com.bms.domain;

import lombok.Getter;

@Getter
public class City {
    private final String id;
    private final String name;
    private final String state;

    public City(String id, String name, String state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }
}
