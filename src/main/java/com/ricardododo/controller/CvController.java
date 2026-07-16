package com.ricardododo.controller;

import com.lowagie.text.DocumentException;
import com.ricardododo.dto.CurriculumDto;
import com.ricardododo.dto.EducationDto;
import com.ricardododo.dto.ExperienceDto;
import com.ricardododo.entity.Curriculum;
import com.ricardododo.service.CurriculumService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
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
    public String saveCV(@ModelAttribute("curriculumDto") CurriculumDto dto,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) throws IOException {

        MultipartFile photoFile = dto.getPhotoFile();

        if (photoFile != null && !photoFile.isEmpty()) {
            String originalFilename = photoFile.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path destinationFile = uploadPath.resolve(filename);
            Files.write(destinationFile, photoFile.getBytes());

            dto.setPhotoUrl("/uploads/" + filename);
        }

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
        dto.setPhotoUrl(curriculum.getPhotoUrl());
        dto.setTemplateName(curriculum.getTemplateName());
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
        String userEmail = auth.getName();
        Curriculum curriculum = curriculumService.getCurriculumByIdAndUser(id, userEmail)
                .orElseThrow(() -> new RuntimeException("CV no encontrado con ID: " + id +
                        " para este usuario"));
        model.addAttribute("curriculum", curriculum);
        return resolveTemplateName(curriculum.getTemplateName());
    }

    private String resolveTemplateName(String templateName) {
        if ("modern".equals(templateName)) {
            return "cv-template-modern";
        } else if ("minimalist".equals(templateName)) {
            return "cv-template-minimalist";
        }
        return "cv-template";
    }

    @GetMapping("/download-pdf/{id}")
    public ResponseEntity<byte[]> downloadPDF(@PathVariable Long id, Authentication auth)
            throws DocumentException, IOException {
        String userEmail = auth.getName();
        Curriculum curriculum = curriculumService.getCurriculumByIdAndUser(id, userEmail)
                .orElseThrow(() -> new RuntimeException("CV no encontrado"));

        // Resolver foto a ruta absoluta file:// para que Flying Saucer pueda cargarla
        if (curriculum.getPhotoUrl() != null && !curriculum.getPhotoUrl().isEmpty()) {
            String photoFilename = curriculum.getPhotoUrl().replace("/uploads/", "");
            Path photoPath = Paths.get(System.getProperty("user.dir"), "uploads", photoFilename);
            if (Files.exists(photoPath)) {
                curriculum.setPhotoUrl(photoPath.toUri().toString());
            }
        }

        // Procesar la plantilla Thymeleaf
        Context context = new Context();
        context.setVariable("curriculum", curriculum);
        String templateFile = resolveTemplateName(curriculum.getTemplateName());
        String htmlContent = templateEngine.process(templateFile, context);

        // Convertir HTML a PDF
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        String baseUrl = Paths.get(System.getProperty("user.dir")).toUri().toString();
        renderer.setDocumentFromString(htmlContent, baseUrl);
        renderer.layout();
        renderer.createPDF(pdfStream);

        byte[] pdfBytes = pdfStream.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "cv_" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}
