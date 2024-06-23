package mk.ukim.finki.molbi.service.impl;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import mk.ukim.finki.molbi.model.base.Course;
import mk.ukim.finki.molbi.model.base.Professor;
import mk.ukim.finki.molbi.model.base.Student;
import mk.ukim.finki.molbi.model.base.User;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.enums.AppRole;
import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.enums.SemesterType;
import mk.ukim.finki.molbi.model.enums.UserRole;
import mk.ukim.finki.molbi.model.exceptions.*;
import mk.ukim.finki.molbi.model.requests.LateCourseEnrollmentStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import mk.ukim.finki.molbi.repository.*;
import mk.ukim.finki.molbi.service.inter.LateCourseEnrollmentStudentRequestService;
import mk.ukim.finki.molbi.service.inter.RequestSessionService;
import mk.ukim.finki.molbi.service.inter.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mk.ukim.finki.molbi.service.specification.FieldFilterSpecification.filterEquals;

@Service
@AllArgsConstructor
public class LateCourseEnrollmentStudentRequestServiceImpl implements LateCourseEnrollmentStudentRequestService {

    private final LateCourseEnrollmentStudentRequestRepository requestRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ProfessorRepository professorRepository;
    private final UserService userService;
    private final EmailRepository emailRepository;
    private final RequestSessionService requestSessionService;

    @Override
    @Transactional
    public Page<LateCourseEnrollmentStudentRequest> filterAndPaginateRequests(Long sessionId,
                                                                              Integer pageNum,
                                                                              Integer results,
                                                                              Boolean professorApproved,
                                                                              Boolean isApproved,
                                                                              Boolean isProcessed,
                                                                              String student,
                                                                              String professor) {
        if (userService.getUserRole().equals(AppRole.PROFESSOR.roleName())) {
            sessionId = null;
            professor = userService.getUserId();
        }

        return findPage(sessionId, pageNum, results, professorApproved, isApproved, isProcessed, student, professor);
    }

    @Override
    @Transactional
    public List<LateCourseEnrollmentStudentRequest> getAllRequestForLoggedInStudent(String index) {
        return requestRepository.findByStudentIndex(index);
    }

    @Override
    public Page<LateCourseEnrollmentStudentRequest> getAllRequestsForLoggedInProfessor(String professor,
                                                                                       String subjectId,
                                                                                       SemesterType semesterType,
                                                                                       int page,
                                                                                       int size) {
        Specification<LateCourseEnrollmentStudentRequest> spec = Specification.where(
                filterEquals(LateCourseEnrollmentStudentRequest.class, "professor.id", professor)
        );

        PageRequest pageRequest = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "professor.id"));

        return requestRepository.findAll(spec, pageRequest);
    }

    @Override
    public LateCourseEnrollmentStudentRequest findById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new LateCourseEnrollmentStudentRequestNotFoundException(id));
    }

    @Override
    public void create(StudentRequestDto dto) {
        if (dto.getCourse() == null || dto.getProfessor().isEmpty() || dto.getDescription().isEmpty()) {
            throw new AllFieldsNotFilledException();
        }

        RequestSession session = requestSessionService.getActiveSessionByType(RequestType.LATE_COURSE_ENROLLMENT);

        Student student = studentRepository.findById(dto.getStudent())
                .orElseThrow(() -> new StudentNotFoundException(dto.getStudent()));
        String description = dto.getDescription();

        Course course = courseRepository.findById(dto.getCourse())
                .orElseThrow(() -> new CourseNotFoundException(dto.getCourse()));

        Professor professor = professorRepository.findById(dto.getProfessor())
                .orElseThrow(() -> new ProfessorNotFoundException(dto.getProfessor()));

        LateCourseEnrollmentStudentRequest request = new LateCourseEnrollmentStudentRequest();

        request.setStudent(student);
        request.setDescription(description);
        request.setRequestSession(session);
        request.setDateCreated(LocalDate.now());
        request.setCourse(course);
        request.setProfessor(professor);
        request.setProfessorApproved(false);
        request.setIsProcessed(false);
        requestRepository.save(request);
    }

    @Override
    public void edit(Long id, StudentRequestDto dto) {

        LateCourseEnrollmentStudentRequest request = findById(id);

        if (request.getProfessorApproved()) {
            throw new IllegalStateException("Cannot edit a reviewed request");
        }

        if (dto.getCourse() == null || dto.getProfessor().isEmpty() || dto.getDescription().isEmpty()) {
            throw new AllFieldsNotFilledException();
        }

        Course course = courseRepository.findById(dto.getCourse())
                .orElseThrow(() -> new CourseNotFoundException(dto.getCourse()));

        Professor professor = professorRepository.findById(dto.getProfessor())
                .orElseThrow(() -> new ProfessorNotFoundException(dto.getProfessor()));

        request.setCourse(course);
        request.setProfessor(professor);
        request.setDescription(dto.getDescription());
        request.setDateCreated(LocalDate.now());
        requestRepository.save(request);
    }

    @Override
    public void delete(Long id) {
        LateCourseEnrollmentStudentRequest request = findById(id);
        if (request.getProfessorApproved()) {
            throw new IllegalStateException("Cannot delete a reviewed request");
        }
        requestRepository.deleteById(id);
    }

    @Override
    public void processApprovalByProfessor(Long id) {
        LateCourseEnrollmentStudentRequest request = findById(id);
        if (request.canBeApprovedByProfessor()) {
            request.setProfessorApproved(true);
            requestRepository.save(request);
            sendMailToAAViceDeanAfterProfessorApproval(request);
            sendMailToStudentOnStatusChange(request);
        }
    }

    @Override
    public void updateStatus(Long id, Boolean action, String rejectReason) throws MessagingException {
        LateCourseEnrollmentStudentRequest request = findById(id);
        if (action && (!request.canBeApproved() || (request.getIsApproved() != null && request.getIsApproved()))) {
            throw new RequestNotApprovedByProfessorOrAlreadyApprovedException(request);
        }

        if (!action && !request.canBeRejected()) {
            throw new RequestAlreadyProcessedOrAlreadyRejectedException(request);
        }

        request.setIsApproved(action);
        request.setRejectionReason(rejectReason);
        requestRepository.save(request);
        if (request.getIsApproved() != null && request.getIsApproved()) {
            sendMailToStudentAdministrationAfterAAViceDeanApproval(request);
        }
        sendMailToStudentOnStatusChange(request);
    }

    @Override
    public void markAsProcessed(Long id) throws MessagingException {
        LateCourseEnrollmentStudentRequest request = findById(id);
        if (request.getIsApproved() == null || !request.canBeMarkedAsProcessed()) {
            throw new RequestNotApprovedOrAlreadyProcessedException(request);
        }
        request.setIsProcessed(true);
        request.setDateProcessed(LocalDate.now());
        requestRepository.save(request);
        sendMailToStudentOnStatusChange(request);
    }

    @Override
    public Long totalRequestBySession(Long sessionId) {
        return requestRepository.countByRequestSession_Id(sessionId);
    }

    @Override
    public List<LateCourseEnrollmentStudentRequest> listAllForSession(RequestSession session) {
        return requestRepository.findByRequestSession(session);
    }

    @Override
    public void sendMailsToProfessors() {
        List<RequestSession> endedSessions = requestSessionService.getJustEndedSessions();
        List<LateCourseEnrollmentStudentRequest> lceStudentRequests = new ArrayList<>();
        for (RequestSession endedSession : endedSessions) {
            lceStudentRequests.addAll(listAllForSession(endedSession));
        }
        for (LateCourseEnrollmentStudentRequest lceStudentRequest : lceStudentRequests) {
            String[] to = {lceStudentRequest.getProfessor().getEmail()};
            Map<String, Object> model = new HashMap<>();
            model.put("professor", lceStudentRequest.getProfessor().getName());
            model.put("index", lceStudentRequest.getStudent().getIndex());
            model.put("course", lceStudentRequest.getCourse().getJoinedSubject().getName());
            emailRepository.sendMailAsync(to, "TestLceMailToProfessor", "LATE_COURSE_ENROLLMENT-email-to-professor-template", null, model, null);
        }
    }

    @Override
    public void sendMailToAAViceDeanAfterProfessorApproval(LateCourseEnrollmentStudentRequest request) {
        User aaViceDean = userService.findUsersByRole(UserRole.ACADEMIC_AFFAIR_VICE_DEAN).stream().findFirst().orElseThrow();
        String[] to = {aaViceDean.getEmail()};
        Map<String, Object> model = new HashMap<>();
        model.put("aavicedean", aaViceDean.getName());
        model.put("index", request.getStudent().getIndex());
        model.put("type", request.getRequestSession().getRequestType());
        emailRepository.sendMailAsync(to, "TestMailToAAViceDean", "email-to-aavicedean-template", null, model, null);
    }

    @Override
    public void sendMailToStudentAdministrationAfterAAViceDeanApproval(LateCourseEnrollmentStudentRequest request) {
        String[] to = (String[]) userService.findUsersByRole(UserRole.STUDENT_ADMINISTRATION).stream().map(User::getEmail).toArray();
        Map<String, Object> model = new HashMap<>();
        model.put("index", request.getStudent().getIndex());
        model.put("type", request.getRequestSession().getRequestType());
        emailRepository.sendMailAsync(to, "TestMailToStudentAdministration", "email-to-studentadmin-template", null, model, null);
    }

    @Override
    public void sendMailToStudentOnStatusChange(LateCourseEnrollmentStudentRequest request) {
        String[] to = {request.getStudent().getEmail()};
        Map<String, Object> model = new HashMap<>();
        model.put("id", request.getId());
        model.put("index", request.getStudent().getIndex());
        model.put("type", request.getRequestSession().getRequestType());
        String status;
        if (request.getIsProcessed()) {
            status = "ПРОЦЕСИРАНА";
        } else if (request.getIsApproved() != null) {
            status = request.getIsApproved() ? "OДОБРЕНА" : "ОДБИЕНА";
        } else {
            status = "ОДОБРЕНА ОД ПРОФЕСОР";
        }
        model.put("status", status);
        emailRepository.sendMailAsync(to, "TestMailToStudent", "email-to-student-template", null, model, null);
    }

    private Page<LateCourseEnrollmentStudentRequest> findPage(Long sessionId,
                                                              Integer pageNum,
                                                              Integer results,
                                                              Boolean professorApproved,
                                                              Boolean isApproved,
                                                              Boolean isProcessed,
                                                              String student,
                                                              String professor) {


        Specification<LateCourseEnrollmentStudentRequest> spec = Specification
                .where(filterEquals(LateCourseEnrollmentStudentRequest.class, "requestSession.id", sessionId))
                .and(filterEquals(LateCourseEnrollmentStudentRequest.class, "professorApproved", professorApproved))
                .and(filterEquals(LateCourseEnrollmentStudentRequest.class, "isApproved", isApproved))
                .and(filterEquals(LateCourseEnrollmentStudentRequest.class, "isProcessed", isProcessed))
                .and(filterEquals(LateCourseEnrollmentStudentRequest.class, "student.index", student))
                .and(filterEquals(LateCourseEnrollmentStudentRequest.class, "professor.id", professor));

        return requestRepository.findAll(spec, getPageRequest(pageNum, results));
    }

    private PageRequest getPageRequest(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize, Sort.Direction.ASC, "id");
    }
}





