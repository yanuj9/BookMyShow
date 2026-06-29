package com.bms.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Seat {
    private final String seatId;
    private final String seatNumber;
    private final double price;
    @Setter
    private SeatStatus status;
    @Setter
    private String bookedBy;

    public Seat(String seatId, String seatNumber, double price) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
        this.bookedBy = null;
    }
}
