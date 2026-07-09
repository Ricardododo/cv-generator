package com.ricardododo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumDto {

    private Long id;
    private String cvName;
    private String fullName;
    private String jobTitle;
    private String email;
    private String phone;
    private String address;
    private String summary;
    private String photoUrl;

    List<ExperienceDto> experiences;
    List<EducationDto> educations;
}
