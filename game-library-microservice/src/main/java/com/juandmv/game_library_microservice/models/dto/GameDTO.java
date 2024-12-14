package com.juandmv.game_library_microservice.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameDTO {
    @JsonProperty("id")
    private Long igdbId;

    @JsonProperty("name")
    private String title;

    @JsonProperty("summary")
    private String description;

    @JsonProperty("cover")
    private Cover cover;

    @JsonProperty("first_release_date")
    private Long releaseDate;

    @JsonProperty("involved_companies")
    private List<InvolvedCompany> involvedCompanies;

    @JsonProperty("platforms")
    private List<Platform> platforms;


    // Clases internas
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cover {
        @JsonProperty("url")
        private String url;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvolvedCompany {
        @JsonProperty("company")
        private Company company;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Company {
        @JsonProperty("name")
        private String name;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Platform {
        @JsonProperty("name")
        private String name;
    }
}