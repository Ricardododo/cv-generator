package com.ricardododo.service;

import com.ricardododo.dto.UserRegistrationDto;
import com.ricardododo.entity.User;
import com.ricardododo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

//Autenticación y Registro
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //Spring Security

    //Inyección por constructor
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //Registrar un usuario
    public User registerUser(UserRegistrationDto dto){
        //Verificar si el email ya existe
        if(userRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new RuntimeException("Email ya registrado: " + dto.getEmail());
        }
        //Crear nueva entidad User
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));//Encriptar
        user.setRoles("ROLE_USER"); //Rol por defecto
        user.setEnabled(true);       //Habilitado por defecto

        //Guardar en la BD
        return userRepository.save(user);
    }


}
