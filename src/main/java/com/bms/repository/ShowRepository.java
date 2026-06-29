package com.bms.repository;

import com.bms.domain.Show;

/**
 * The Contract. The Service layer only talks to this interface.
 * It doesn't care if the data is stored in RAM, Postgres, or a text file.
 */
public interface ShowRepository {
    void save(Show show);
    Show findById(String showId);
}
