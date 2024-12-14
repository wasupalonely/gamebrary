package com.juandmv.rating_microservice.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRatingDTO {

    private Integer rating;
    private String comment;
}
