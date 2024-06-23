package mk.ukim.finki.molbi.web;


import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import mk.ukim.finki.molbi.config.FacultyUserDetails;
import mk.ukim.finki.molbi.model.requests.LateCourseEnrollmentStudentRequest;
import mk.ukim.finki.molbi.service.inter.LateCourseEnrollmentStudentRequestService;
import mk.ukim.finki.molbi.service.inter.ProfessorService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
@RequestMapping("/professor")
public class ProfessorController {

    final ProfessorService professorService;
    final LateCourseEnrollmentStudentRequestService lateCourseEnrollmentStudentRequestService;

    @GetMapping
    public String getAllTeacherRequests(Model model,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "20") Integer results,
                                        @AuthenticationPrincipal FacultyUserDetails principal) {

        Page<LateCourseEnrollmentStudentRequest> page;
        String professorId = principal.getUsername();
        page = lateCourseEnrollmentStudentRequestService.getAllRequestsForLoggedInProfessor(
                professorId,
                null,
                null,
                pageNum,
                results
        );

        model.addAttribute("page", page);
        model.addAttribute("professorId", professorId);

        return "teacher-requests/listTeacherRequests";
    }

    @PostMapping("/professorApprove/{id}")
    public String approveByProfessorRequest(@PathVariable Long id) {
        try {
            lateCourseEnrollmentStudentRequestService.processApprovalByProfessor(id);
        } catch (MessagingException e) {
            System.err.println(e.getMessage());
        }
        return "redirect:/professor";
    }
}
