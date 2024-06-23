package mk.ukim.finki.molbi.web;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.molbi.config.FacultyUserDetails;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.exceptions.NotAuthorizedForThisActionException;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import mk.ukim.finki.molbi.model.requests.StudentRequest;
import mk.ukim.finki.molbi.service.inter.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {

    private final RequestSessionService sessionService;
    private final CourseService courseService;
    private final ProfessorService professorService;
    private final StudentRequestService studentRequestService;
    private final StudyProgramService studyProgramService;

    @GetMapping
    public String listActiveRequestSessions(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer results,
                                            Model model) {
        Page<RequestSession> page = sessionService.getAllActiveSessions(pageNum, results);
        model.addAttribute("page", page);
        return "student-requests/request-sessions";
    }

    @GetMapping("/my_requests")
    public String listStudentRequests(@AuthenticationPrincipal FacultyUserDetails principal,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer results,
                                      @RequestParam(required = false) RequestType requestType,
                                      Model model) {
        Page<StudentRequest> page = studentRequestService
                .getAllRequestsByStudentIndex(principal.getUsername(), pageNum, results, requestType);
        model.addAttribute("page", page);
        model.addAttribute("requestTypes", RequestType.values());
        return "student-requests/list";
    }

    @ResponseBody
    @GetMapping("/details/{id}")
    public ResponseEntity<StudentRequest> getRequestById(@PathVariable Long id, @AuthenticationPrincipal FacultyUserDetails principal) {
        try {
            StudentRequest request = studentRequestService.getRequestById(principal.getUsername(), id);
            return ResponseEntity.ok(request);
        } catch (NotAuthorizedForThisActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }


    @GetMapping({"/{requestType}/apply", "/{requestType}/edit/{id}"})
    public String showFormPage(@PathVariable RequestType requestType,
                               @PathVariable(required = false) Long id,
                               @RequestParam(required = false) String error,
                               @AuthenticationPrincipal FacultyUserDetails principal,
                               Model model) {
        if (error != null) {
            model.addAttribute("hasError", true);
            model.addAttribute("error", error);
        }

        if (id != null) {
            try {
                StudentRequest request = studentRequestService.getRequestById(principal.getUsername(), id);
                model.addAttribute("request", request);
            } catch (NotAuthorizedForThisActionException ex) {
                return "redirect:/student/my_requests";
            }
        }

        model.addAttribute("courses", courseService.listBySemesterState());
        model.addAttribute("professors", professorService.listAll());
        model.addAttribute("requestType", requestType);
        model.addAttribute("studyPrograms", studyProgramService.listAll());
        return requestType + "/form";
    }


    @PostMapping("/{requestType}/apply")
    public String applyForRequest(@ModelAttribute StudentRequestDto dto,
                                  @PathVariable RequestType requestType,
                                  @AuthenticationPrincipal FacultyUserDetails principal,
                                  @RequestParam(required = false) Long id) {
        try {
            if (id != null) {
                studentRequestService.edit(principal.getUsername(), dto, id);
            } else {
                studentRequestService.create(requestType, principal.getUsername(), dto);
            }
            return "redirect:/student/my_requests";
        } catch (RuntimeException e) {
            if (id != null) {
                return "redirect:/student/" + requestType + "/edit/" + id + "?error=" + e.getMessage();
            } else {
                return "redirect:/student/" + requestType + "/apply?error=" + e.getMessage();
            }
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteRequest(@PathVariable Long id, @AuthenticationPrincipal FacultyUserDetails principal) {
        try {
            studentRequestService.deleteRequest(id, principal.getUsername());
            return "redirect:/student/my_requests";
        } catch (NotAuthorizedForThisActionException ex) {
            return "redirect:/student/my_requests";
        }
    }
}
