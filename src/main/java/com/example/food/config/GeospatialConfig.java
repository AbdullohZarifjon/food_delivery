package com.example.food.config;


import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeospatialConfig {

    @Bean
    public GeometryFactory geometryFactory() {
        // SRID 4326 (WGS84) - bu GPS uchun xalqaro standart
        // PrecisionModel() - hisob-kitob aniqligi uchun
        return new GeometryFactory(new PrecisionModel(), 4326);
    }
}
