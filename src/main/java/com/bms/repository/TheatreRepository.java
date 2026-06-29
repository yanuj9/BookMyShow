package com.bms.repository;

import com.bms.domain.Theatre;
import com.bms.domain.City;
import java.util.List;
import java.util.Optional;

public interface TheatreRepository {
    void saveCity(City city);
    void saveTheatre(Theatre theatre);
    Optional<City> findCityByName(String cityName);
    List<Theatre> findTheatresByCityId(String cityId);
}
