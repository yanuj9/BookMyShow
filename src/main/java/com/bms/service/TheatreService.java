package com.bms.service;

import com.bms.domain.City;
import com.bms.domain.Theatre;
import com.bms.repository.TheatreRepository;
import com.bms.exception.UnservicableLocationException;
import java.util.List;
import java.util.Optional;

public class TheatreService {
    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    /**
     * Resolves theatres and their internal screens based on user's chosen city name.
     */
    public List<Theatre> getTheatresInCity(String cityName) {
        Optional<City> cityOpt = theatreRepository.findCityByName(cityName);
        
        if (cityOpt.isEmpty()) {
            throw new UnservicableLocationException("We currently do not service the city: " + cityName);
        }

        City city = cityOpt.get();
        return theatreRepository.findTheatresByCityId(city.getId());
    }
}
