package com.API.API_limiter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @GetMapping("/current")
    public ResponseEntity<Map<String, String>> getCurrentWeather() {
        return ResponseEntity.ok(Map.of(
                "location", "Kochi, India",
                "temperature", "30°C",
                "condition", "Monsoon Rain",
                "humidity", "85%"));
    }

    @GetMapping("/forecast")
    public ResponseEntity<Map<String, String>> getForecast() {
        return ResponseEntity.ok(Map.of(
                "tomorrow", "Rainy",
                "day_after", "Cloudy"));
    }
}
