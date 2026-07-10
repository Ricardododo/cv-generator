package com.ricardododo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    //Inicializar las listas para que Spring pueda mapear los campos
    List<ExperienceDto> experiences =  new ArrayList<>();
    List<EducationDto> educations = new ArrayList<>();
}
