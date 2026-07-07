package com.ricardododo.repository;

import com.ricardododo.entity.Curriculum;
import com.ricardododo.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {

    //obtener todos los registros de educación en un CV específico
    List<Education> findAllByCurriculum(Curriculum curriculum);

}
