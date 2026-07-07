package com.ricardododo.repository;

import com.ricardododo.entity.Curriculum;
import com.ricardododo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

    //obtener lista de CVs por usuario
    List<Curriculum> findByUser(User user);

    //obtener 1 cv concreto de 1 usuario (Eliminar O Editar)
    Optional<Curriculum> findByIdAndUser(Long id, User user);
}
