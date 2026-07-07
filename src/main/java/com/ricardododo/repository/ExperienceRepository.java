package com.ricardododo.repository;

import com.ricardododo.entity.Curriculum;
import com.ricardododo.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    //Obtener toda la experiencia en un CV específico
    List<Experience> findAllByCurriculum(Curriculum curriculum);

    //Obternerlas ordenadas por fecha de inicio
    List<Experience> findByCurriculumOrderByStartDateAsc(Curriculum curriculum);
}
