package com.bms.domain;

import lombok.Getter;
import java.util.List;

@Getter
public class Theatre {
    private final String id;
    private final String name;
    private final String address;
    private final String cityId; // Foreign key reference to City
    private final List<Screen> screens; // Composition relationship

    public Theatre(String id, String name, String address, String cityId, List<Screen> screens) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.cityId = cityId;
        this.screens = screens;
    }
}
