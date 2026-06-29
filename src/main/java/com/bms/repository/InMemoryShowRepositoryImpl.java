package com.bms.repository;

import com.bms.domain.Show;
import java.util.HashMap;
import java.util.Map;

/**
 * Used for local testing and simulations.
 */
public class InMemoryShowRepositoryImpl implements ShowRepository {
    private final Map<String, Show> db = new HashMap<>();

    @Override
    public void save(Show show) {
        db.put(show.getShowId(), show);
    }

    @Override
    public Show findById(String showId) {
        return db.get(showId);
    }
}
