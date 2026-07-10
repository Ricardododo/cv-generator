package com.ricardododo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDto {

    private Long id;
    private String company;
    private String position;
    private String startDate;
    private String endDate;
    private String description;
}
