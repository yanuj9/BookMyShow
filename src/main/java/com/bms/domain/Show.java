package com.bms.domain;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class Show {
    private final String showId;
    private final String movieId;
    private final String screenId;
    private final LocalDateTime startTime;
    private final Map<String, Seat> seats; // Map of SeatId -> Seat

    public Show(String showId, String movieId, String screenId, LocalDateTime startTime, Map<String, Seat> seats) {
        this.showId = showId;
        this.movieId = movieId;
        this.screenId = screenId;
        this.startTime = startTime;
        this.seats = seats;
    }
}
