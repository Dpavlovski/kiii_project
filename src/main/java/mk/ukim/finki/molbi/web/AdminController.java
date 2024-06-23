package mk.ukim.finki.molbi.web;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.molbi.model.dtos.RequestSessionDto;
import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.requests.*;
import mk.ukim.finki.molbi.service.inter.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final RequestSessionService requestSessionService;
    private final SemesterService semesterService;
    private final StudentRequestService requestService;
    private final GeneralStudentRequestService genService;
    private final LateCourseEnrollmentStudentRequestService lceService;
    private final ChangeStudyProgramStudentRequestService spcService;
    private final CourseEnrollmentWithoutRequirementsStudentRequestService cewrService;
    private final CourseGroupChangeStudentRequestService cgcService;
    private final CourseService courseService;
    private final ProfessorService professorService;
    private final StudyProgramService studyProgramService;

    @GetMapping
    public String listRequestSessions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer results,
            @RequestParam(required = false) LocalDateTime timeFrom,
            @RequestParam(required = false) LocalDateTime timeTo,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) Boolean active,
            Model model) {

        Page<RequestSession> page = requestSessionService
                .filterAndPaginateSessions(pageNum, results, timeFrom, timeTo, semester, requestType, active);
        model.addAttribute("sessionPage", page);
        model.addAttribute("semesters", semesterService.findAll());
        model.addAttribute("requestTypes", RequestType.values());

        return "admin-sessions/list";
    }

    @GetMapping({"/add", "/edit/{id}"})
    public String showFormPage(@PathVariable(required = false) Long id, Model model) {
        model.addAttribute("semesters", semesterService.findAll());
        model.addAttribute("requestTypes", RequestType.values());


        if (id != null) {
            RequestSession session = this.requestSessionService.findById(id);
            model.addAttribute("requestSession", session);
        }

        return "admin-sessions/form";
    }

    @PostMapping("/save")
    public String saveRequestSession(
            @RequestParam(required = false) Long id,
            @ModelAttribute RequestSessionDto dto
    ) {
        requestSessionService.save(id, dto);
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteRequestSession(@PathVariable Long id) {
        requestSessionService.delete(id);
        return "redirect:/admin";
    }

    @PostMapping("/updateStatus/{sessionId}/{id}")
    public String updateStatus(@PathVariable Long id,
                               @PathVariable Long sessionId,
                               @RequestParam Boolean action,
                               @RequestParam(required = false) String rejectionReason,
                               HttpSession session) throws MessagingException {
        RequestSession requestSession = requestSessionService.findById(sessionId);

        requestService.updateStatus(requestSession, id, action, rejectionReason);
        session.setAttribute("rejectionReason", rejectionReason);
        return "redirect:/admin/" + requestSession.getRequestType() + "/" + requestSession.getId();
    }

    @PostMapping("/process/{sessionId}/{id}")
    public String markAsProcessed(@PathVariable Long id,
                                  @PathVariable Long sessionId) throws MessagingException {
        RequestSession requestSession = requestSessionService.findById(sessionId);

        requestService.markAsProcessed(requestSession, id);
        return "redirect:/admin/" + requestSession.getRequestType() + "/" + requestSession.getId();
    }


    @GetMapping("/LATE_COURSE_ENROLLMENT/{sessionId}")
    public String showLCERequests(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer results,
            @RequestParam(required = false) Boolean professorApproved,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) Boolean isProcessed,
            @RequestParam(required = false) String student,
            @RequestParam(required = false) String professor,
            HttpSession session,
            Model model) {

        Page<LateCourseEnrollmentStudentRequest> page = lceService.filterAndPaginateRequests(
                sessionId, pageNum, results, professorApproved, isApproved, isProcessed, student, professor);
        model.addAttribute("page", page);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("courses", courseService.listBySemesterState());
        model.addAttribute("professors", professorService.listAll());
        model.addAttribute("rejectionReason", session.getAttribute("rejectionReason"));
        return "LATE_COURSE_ENROLLMENT/list";
    }


    @GetMapping("/GENERAL/{sessionId}")
    public String showGENRequests(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer results,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) Boolean isProcessed,
            @RequestParam(required = false) String student,
            HttpSession session,
            Model model) {

        Page<GeneralStudentRequest> page = genService.filterAndPaginateRequests(
                sessionId, pageNum, results, isApproved, isProcessed, student);
        model.addAttribute("page", page);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("rejectionReason", session.getAttribute("rejectionReason"));
        return "GENERAL/list";
    }

    @GetMapping("/STUDY_PROGRAM_CHANGE/{sessionId}")
    public String showSPCRequests(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer results,
            @RequestParam(required = false) String oldStudyProgram,
            @RequestParam(required = false) String newStudyProgram,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) Boolean isProcessed,
            @RequestParam(required = false) String student,
            HttpSession session,
            Model model) {

        Page<ChangeStudyProgramStudentRequest> page = spcService.filterAndPaginateRequests(
                sessionId, pageNum, results, oldStudyProgram, newStudyProgram, isApproved, isProcessed, student);
        model.addAttribute("page", page);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("rejectionReason", session.getAttribute("rejectionReason"));
        model.addAttribute("studyPrograms", studyProgramService.listAll());
        return "STUDY_PROGRAM_CHANGE/list";
    }


    @GetMapping("/COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS/{sessionId}")
    public String showCEWRRequests(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer results,
            @RequestParam(required = false) Long course,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) Boolean isProcessed,
            @RequestParam(required = false) String student,
            HttpSession session,
            Model model) {

        Page<CourseEnrollmentWithoutRequirementsStudentRequest> page = cewrService.filterAndPaginateRequests(
                sessionId, pageNum, results, course, isApproved, isProcessed, student);
        model.addAttribute("page", page);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("rejectionReason", session.getAttribute("rejectionReason"));
        model.addAttribute("courses", courseService.listBySemesterState());
        return "COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS/list";
    }


    @GetMapping("/COURSE_GROUP_CHANGE/{sessionId}")
    public String showSPCRequests(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer results,
            @RequestParam(required = false) Long course,
            @RequestParam(required = false) String currentProfessor,
            @RequestParam(required = false) String newProfessor,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) Boolean isProcessed,
            @RequestParam(required = false) String student,
            HttpSession session,
            Model model) {

        Page<CourseGroupChangeStudentRequest> page = cgcService.filterAndPaginateRequests(
                sessionId, pageNum, results, course, currentProfessor, newProfessor, isApproved, isProcessed, student);
        model.addAttribute("page", page);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("rejectionReason", session.getAttribute("rejectionReason"));
        model.addAttribute("courses", courseService.listBySemesterState());
        model.addAttribute("professors", professorService.listAll());
        return "COURSE_GROUP_CHANGE/list";
    }

}


