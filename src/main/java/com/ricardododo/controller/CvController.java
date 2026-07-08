package com.ricardododo.controller;

import com.ricardododo.dto.CurriculumDto;
import com.ricardododo.entity.Curriculum;
import com.ricardododo.service.CurriculumService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
public class CvController {

    private final CurriculumService curriculumService;

    public CvController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    @PostMapping("/save-cv")
    public String saveCV(@ModelAttribute CurriculumDto dto, Authentication auth, RedirectAttributes redirectAttributes) {
        String userEmail = auth.getName();
        try {
            curriculumService.saveCurriculum(dto, userEmail);
            redirectAttributes.addFlashAttribute("success", "CV guardado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/my-cvs";
    }

    @GetMapping("/my-cvs")
    public String listMyCVs(Authentication auth, Model model) {
        String userEmail = auth.getName();
        List<Curriculum> cvs = curriculumService.getCurriculumsByUser(userEmail);
        model.addAttribute("cvs", cvs);
        return "my-cvs";
    }

    @GetMapping("/edit-cv/{id}")
    public String editCV(@PathVariable Long id, Authentication auth, Model model) {
        String userEmail = auth.getName();
        Curriculum cv = curriculumService.getCurriculumByIdAndUser(id, userEmail)
                .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        model.addAttribute("cv", cv);
        return "dashboard"; // Podrías reutilizar dashboard con datos precargados
    }

    @GetMapping("/delete-cv/{id}")
    public String deleteCV(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        String userEmail = auth.getName();
        try {
            curriculumService.deletedCurriculum(id, userEmail);
            redirectAttributes.addFlashAttribute("success", "CV eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar.");
        }
        return "redirect:/my-cvs";
    }

    @GetMapping("/download-pdf/{id}")
    public String downloadPDF(@PathVariable Long id, Authentication auth) {
        // Lógica para generar PDF (más adelante)
        return "redirect:/my-cvs"; // Temporal
    }
}
