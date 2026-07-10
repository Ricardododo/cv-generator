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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//Gestion de CVs
@Service
public class CurriculumService {

     private final CurriculumRepository curriculumRepository;
     private final ExperienceRepository experienceRepository;
     private final EducationRepository educationRepository;
     private final UserRepository userRepository;

     public CurriculumService(CurriculumRepository curriculumRepository, ExperienceRepository experienceRepository, EducationRepository educationRepository, UserRepository userRepository) {
          this.curriculumRepository = curriculumRepository;
          this.experienceRepository = experienceRepository;
          this.educationRepository = educationRepository;
          this.userRepository = userRepository;
     }

     //Guardar el cv
     @Transactional
     public Curriculum saveCurriculum(CurriculumDto dto, String userEmail){
          //1. Buscar el usuario por email
          User user = userRepository.findByEmail(userEmail)
                  .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + userEmail));

          Curriculum curriculum;
          //2. verificar si el cv tiene o no id
         if (dto.getId() == null){
             curriculum = new Curriculum();
             curriculum.setUser(user);
             //Inicializar listas de nuevo
             curriculum.setExperiences(new ArrayList<>());
             curriculum.setEducations(new ArrayList<>());
         }else{
             curriculum = curriculumRepository.findByIdAndUser(dto.getId(), user)
                     .orElseThrow(() -> new RuntimeException("CV no encontrado para este usuario"));

             //limpiar listas antiguas para reemplazarlas (si cascade + orphanRemoval)
             curriculum.getExperiences().clear();
             curriculum.getEducations().clear();
         }
          //actualizar datos
          curriculum.setCvName(dto.getCvName());
          curriculum.setFullName(dto.getFullName());
          curriculum.setJobTitle(dto.getJobTitle());
          curriculum.setEmail(dto.getEmail());
          curriculum.setPhone(dto.getPhone());
          curriculum.setAddress(dto.getAddress());
          curriculum.setSummary(dto.getSummary());
          curriculum.setPhotoUrl(dto.getPhotoUrl());

          //Procesar experiencias si existieran
          if(dto.getExperiences() != null) {
               for (ExperienceDto expDto : dto.getExperiences()) {
                   Experience experience = new Experience();
                   experience.setCompany(expDto.getCompany());
                   experience.setPosition(expDto.getPosition());
                   experience.setStartDate(expDto.getStartDate());
                   experience.setEndDate(expDto.getEndDate());
                   experience.setDescription(expDto.getDescription());
                   experience.setCurriculum(curriculum); //relación bidireccional
                   curriculum.getExperiences().add(experience);
               }
          }
          //Procesar educaciones si existieran
          if(dto.getEducations() != null) {
              for (EducationDto eduDto : dto.getEducations()) {
                  Education education = new Education();
                  education.setInstitution(eduDto.getInstitution());
                  education.setDegree(eduDto.getDegree());
                  education.setYear(eduDto.getYear());
                  education.setCurriculum(curriculum); //relación bidireccional
                  curriculum.getEducations().add(education);
              }
          }
          //Guardar curriculum (con cascade ALL, se guardan los dos exp y edu)
          return curriculumRepository.save(curriculum);
     }

     //obtener todos los curriculums del usuario
     public List<Curriculum> getCurriculumsByUser(String userEmail){
          User user =  userRepository.findByEmail(userEmail)
                  .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + userEmail));
          return curriculumRepository.findByUser(user);
     }

     //Obtener un cv especifico de un usuario, si exite
     public Optional<Curriculum> getCurriculumByIdAndUser(Long curriculumId, String userEmail) {
          User user = userRepository.findByEmail(userEmail)
                  .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + userEmail));
          return curriculumRepository.findByIdAndUser(curriculumId, user);
     }

     //Elimina un cv de usuario (si existe y es del usuario)
    @Transactional
     public void deletedCurriculum(Long curriculumId, String userEmail) {
          //Buscar cv para asegurar que existe y pertenece al usuario
          Curriculum curriculum = getCurriculumByIdAndUser(curriculumId, userEmail)
                  .orElseThrow(() -> new RuntimeException("CV no encontrado con ID: " + curriculumId + " para este usuario"));

          // Eliminar (con cascade, se eliminarán experiencias y educaciones)
          curriculumRepository.delete(curriculum);
     }
}
