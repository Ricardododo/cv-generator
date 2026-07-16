package com.ricardododo.service;

import com.ricardododo.dto.CurriculumDto;
import com.ricardododo.dto.EducationDto;
import com.ricardododo.dto.ExperienceDto;
import com.ricardododo.entity.Curriculum;
import com.ricardododo.entity.Education;
import com.ricardododo.entity.Experience;
import com.ricardododo.entity.User;
import com.ricardododo.repository.CurriculumRepository;
import com.ricardododo.repository.EducationRepository;
import com.ricardododo.repository.ExperienceRepository;
import com.ricardododo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final ExperienceRepository experienceRepository;
    private final EducationRepository educationRepository;
    private final UserRepository userRepository;

    public CurriculumService(CurriculumRepository curriculumRepository,
                             ExperienceRepository experienceRepository,
                             EducationRepository educationRepository,
                             UserRepository userRepository) {
        this.curriculumRepository = curriculumRepository;
        this.experienceRepository = experienceRepository;
        this.educationRepository = educationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Curriculum saveCurriculum(CurriculumDto dto, String userEmail) {
        // 1. Buscar el usuario por email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + userEmail));

        Curriculum curriculum;

        // 2. Verificar si el CV tiene o no id
        if (dto.getId() == null) {
            // Nuevo CV
            curriculum = new Curriculum();
            curriculum.setUser(user);
            curriculum.setExperiences(new ArrayList<>());
            curriculum.setEducations(new ArrayList<>());
        } else {
            // Editar CV existente
            curriculum = curriculumRepository.findByIdAndUser(dto.getId(), user)
                    .orElseThrow(() -> new RuntimeException("CV no encontrado para este usuario"));

            // ----- ELIMINAR FOTO ANTIGUA (SI LA NUEVA ES DIFERENTE) -----
            String oldPhotoUrl = curriculum.getPhotoUrl();
            String newPhotoUrl = dto.getPhotoUrl();
            if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty()
                    && newPhotoUrl != null && !newPhotoUrl.isEmpty()
                    && !oldPhotoUrl.equals(newPhotoUrl)) {
                String oldPhotoFilename = oldPhotoUrl.replace("/uploads/", "");
                Path oldFile = Paths.get(System.getProperty("user.dir"), "uploads", oldPhotoFilename);
                try {
                    Files.deleteIfExists(oldFile);
                } catch (IOException e) {
                    System.out.println("No se pudo eliminar foto antigua: " + oldFile);
                }
            }

            // Limpiar listas antiguas para reemplazarlas (cascade + orphanRemoval)
            curriculum.getExperiences().clear();
            curriculum.getEducations().clear();
        }

        // Actualizar datos
        curriculum.setCvName(dto.getCvName());
        curriculum.setFullName(dto.getFullName());
        curriculum.setJobTitle(dto.getJobTitle());
        curriculum.setEmail(dto.getEmail());
        curriculum.setPhone(dto.getPhone());
        curriculum.setAddress(dto.getAddress());
        curriculum.setSummary(dto.getSummary());
        curriculum.setPhotoUrl(dto.getPhotoUrl());
        curriculum.setTemplateName(dto.getTemplateName());

        // Procesar experiencias
        if (dto.getExperiences() != null) {
            for (ExperienceDto expDto : dto.getExperiences()) {
                Experience experience = new Experience();
                experience.setCompany(expDto.getCompany());
                experience.setPosition(expDto.getPosition());
                experience.setStartDate(expDto.getStartDate());
                experience.setEndDate(expDto.getEndDate());
                experience.setDescription(expDto.getDescription());
                experience.setCurriculum(curriculum);
                curriculum.getExperiences().add(experience);
            }
        }

        // Procesar educaciones
        if (dto.getEducations() != null) {
            for (EducationDto eduDto : dto.getEducations()) {
                Education education = new Education();
                education.setInstitution(eduDto.getInstitution());
                education.setDegree(eduDto.getDegree());
                education.setYear(eduDto.getYear());
                education.setCurriculum(curriculum);
                curriculum.getEducations().add(education);
            }
        }

        System.out.println("PhotoUrl del DTO: " + dto.getPhotoUrl());

        // Guardar curriculum (con cascade ALL, se guardan experiencias y educaciones)
        return curriculumRepository.save(curriculum);
    }

    public List<Curriculum> getCurriculumsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + userEmail));
        return curriculumRepository.findByUser(user);
    }

    public Optional<Curriculum> getCurriculumByIdAndUser(Long curriculumId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + userEmail));
        return curriculumRepository.findByIdAndUser(curriculumId, user);
    }

    @Transactional
    public void deletedCurriculum(Long curriculumId, String userEmail) {
        Curriculum curriculum = getCurriculumByIdAndUser(curriculumId, userEmail)
                .orElseThrow(() -> new RuntimeException("CV no encontrado con ID: " + curriculumId + " para este usuario"));

        // Eliminar foto asociada
        if (curriculum.getPhotoUrl() != null && !curriculum.getPhotoUrl().isEmpty()) {
            String photoFilename = curriculum.getPhotoUrl().replace("/uploads/", "");
            Path photoPath = Paths.get(System.getProperty("user.dir"), "uploads", photoFilename);
            try {
                Files.deleteIfExists(photoPath);
            } catch (IOException e) {
                System.out.println("No se pudo eliminar foto: " + photoPath);
            }
        }

        curriculumRepository.delete(curriculum);
    }
}
