package com.bms.repository;

import com.bms.domain.City;
import com.bms.domain.Theatre;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTheatreRepositoryImpl implements TheatreRepository {
    private final Map<String, City> cityMap = new HashMap<>();
    private final Map<String, Theatre> theatreMap = new HashMap<>();

    @Override
    public void saveCity(City city) {
        cityMap.put(city.getId(), city);
    }

    @Override
    public void saveTheatre(Theatre theatre) {
        theatreMap.put(theatre.getId(), theatre);
    }

    @Override
    public Optional<City> findCityByName(String cityName) {
        return cityMap.values().stream()
                .filter(c -> c.getName().equalsIgnoreCase(cityName))
                .findFirst();
    }

    @Override
    public List<Theatre> findTheatresByCityId(String cityId) {
        return theatreMap.values().stream()
                .filter(t -> t.getCityId().equals(cityId))
                .collect(Collectors.toList());
    }
}
