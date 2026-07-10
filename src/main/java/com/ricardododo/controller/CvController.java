package com.ricardododo.controller;

import com.lowagie.text.DocumentException;
import com.ricardododo.dto.CurriculumDto;
import com.ricardododo.dto.EducationDto;
import com.ricardododo.dto.ExperienceDto;
import com.ricardododo.entity.Curriculum;
import com.ricardododo.service.CurriculumService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class CvController {

    private final CurriculumService curriculumService;
    private final SpringTemplateEngine templateEngine;

    public CvController(CurriculumService curriculumService, SpringTemplateEngine templateEngine) {
        this.curriculumService = curriculumService;
        this.templateEngine = templateEngine;
    }
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(100); // Permite que las listas crezcan
    }

    @PostMapping("/save-cv")
    public String saveCV(@ModelAttribute ("curriculumDto") CurriculumDto dto,
                         Authentication auth,
                         RedirectAttributes redirectAttributes,
                         HttpServletRequest request) {
        // Imprime todos los parámetros recibidos
        System.out.println("=== PARÁMETROS RECIBIDOS ===");
        request.getParameterMap().forEach((key, value) -> {
            System.out.println(key + " = " + Arrays.toString(value));
        });
        System.out.println("=============================");

        System.out.println("DTO recibido: " + dto);
        System.out.println("Experiencias: " + dto.getExperiences());
        System.out.println("Educaciones: " + dto.getEducations());
        String userEmail = auth.getName();
        try {
            curriculumService.saveCurriculum(dto, userEmail);
            redirectAttributes.addFlashAttribute("success", "CV guardado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
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
        Curriculum curriculum = curriculumService.getCurriculumByIdAndUser(id, userEmail)
                .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        //convertir a DTO
        CurriculumDto dto = convertToDto(curriculum); //metodo aux
        model.addAttribute("curriculumDto", dto);
        System.out.println("JobTitle recuperado: " + curriculum.getJobTitle());
        return "dashboard"; //misma vista, pero con datos precargados
    }
    //metodo auxiliar de editCv
    private CurriculumDto convertToDto(Curriculum curriculum){
        CurriculumDto dto = new CurriculumDto();
        dto.setId(curriculum.getId());
        dto.setCvName(curriculum.getCvName());
        dto.setFullName(curriculum.getFullName());
        dto.setJobTitle(curriculum.getJobTitle());
        dto.setEmail(curriculum.getEmail());
        dto.setPhone(curriculum.getPhone());
        dto.setAddress(curriculum.getAddress());
        dto.setSummary(curriculum.getSummary());
        // Mapear listas
        if (curriculum.getExperiences() != null) {
            List<ExperienceDto> expDtos = curriculum.getExperiences().stream()
                    .map(exp -> {
                        ExperienceDto expDto = new ExperienceDto();
                        expDto.setCompany(exp.getCompany());
                        expDto.setPosition(exp.getPosition());
                        expDto.setStartDate(exp.getStartDate());
                        expDto.setEndDate(exp.getEndDate());
                        expDto.setDescription(exp.getDescription());
                        return expDto;
                    })
                    .collect(Collectors.toList());
            dto.setExperiences(expDtos);
        }
        if (curriculum.getEducations() != null) {
            List<EducationDto> eduDtos = curriculum.getEducations().stream()
                    .map(edu -> {
                        EducationDto eduDto = new EducationDto();
                        eduDto.setInstitution(edu.getInstitution());
                        eduDto.setDegree(edu.getDegree());
                        eduDto.setYear(edu.getYear());
                        return eduDto;
                    })
                    .collect(Collectors.toList());
            dto.setEducations(eduDtos);
        }
        return dto;
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

    @GetMapping("/preview-cv/{id}")
    public String previewCv(@PathVariable Long id, Authentication auth, Model model){
        //obtener email del usuario autenticado
        String userEmail = auth.getName();
        //recuperar el CV usando el servicio
        Curriculum curriculum = curriculumService.getCurriculumByIdAndUser(id, userEmail)
                .orElseThrow(() -> new RuntimeException("CV no encontrado con ID: " + id +
                        " para este usuario"));
        //agregar el curriculum al modelo
        model.addAttribute("curriculum", curriculum);
        //devolver la vista a la plantilla
        return "cv-template";
    }

    @GetMapping("/download-pdf/{id}")
    public ResponseEntity<byte[]> downloadPDF(@PathVariable Long id, Authentication auth)
            throws DocumentException, IOException {
        String userEmail = auth.getName();
        Curriculum curriculum = curriculumService.getCurriculumByIdAndUser(id, userEmail)
                .orElseThrow(() -> new RuntimeException("CV no encontrado"));
         //Procesar la plantilla Thymeleaf
        Context context = new Context();
        context.setVariable("curriculum", curriculum);
        String htmlContent = templateEngine.process("cv-template", context);
        //convertir HTML a PDF usando Flying saucer
        ByteArrayOutputStream pdfStream =new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        String baseUrl = "http://localhost:8080"; // En producción, obtener de properties
        renderer.setDocumentFromString(htmlContent, baseUrl);
        renderer.layout();
        renderer.createPDF(pdfStream);

        // devolver el PDF como ResponseEntity
        byte[] pdfBytes = pdfStream.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "cv_" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}
