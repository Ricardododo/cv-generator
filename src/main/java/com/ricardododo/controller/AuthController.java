package com.ricardododo.controller;

import com.ricardododo.dto.CurriculumDto;
import com.ricardododo.dto.UserRegistrationDto;
import com.ricardododo.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //Mostrar el formulario de registro
    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    //Procesar el registro de un nuevo usuario
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDto dto,
                               RedirectAttributes redirectAttributes) {
        try{
            authService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Registro exitoso! Ahora puedes iniciar sesión.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    //Mostrar el formulario de login
    //Spring security maneja la autenticación, pero esta vista muestra mensajes de error y logout
    @GetMapping("/login")
    public String showLoginFrom(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "has cerrado sesión correctamente.");
        }
        return "login";
    }

    //pagina de dashboard tras login exitoso
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model){
        model.addAttribute("userEmail", auth.getName());
        model.addAttribute("curriculumDto", new CurriculumDto());
        return "dashboard";
    }

    //get logout (spring secutiry ya lo maneja pero se puede tener un enlace en la vista html
}
