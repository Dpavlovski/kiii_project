package mk.ukim.finki.molbi.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import mk.ukim.finki.molbi.model.base.Course;
import mk.ukim.finki.molbi.model.base.Student;
import mk.ukim.finki.molbi.model.base.User;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.enums.UserRole;
import mk.ukim.finki.molbi.model.exceptions.*;
import mk.ukim.finki.molbi.model.requests.CourseEnrollmentWithoutRequirementsStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import mk.ukim.finki.molbi.repository.CourseEnrollmentWithoutRequirementsStudentRequestRepository;
import mk.ukim.finki.molbi.repository.CourseRepository;
import mk.ukim.finki.molbi.repository.EmailRepository;
import mk.ukim.finki.molbi.repository.StudentRepository;
import mk.ukim.finki.molbi.service.inter.CourseEnrollmentWithoutRequirementsStudentRequestService;
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
public class CourseEnrollmentWithoutRequirementsStudentRequestImpl
        implements CourseEnrollmentWithoutRequirementsStudentRequestService {

    private final CourseEnrollmentWithoutRequirementsStudentRequestRepository requestRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final RequestSessionService requestSessionService;
    private final UserService userService;
    private final EmailRepository emailRepository;

    @Transactional
    @Override
    public Page<CourseEnrollmentWithoutRequirementsStudentRequest> filterAndPaginateRequests(Long sessionId,
                                                                                             int pageNum,
                                                                                             int results,
                                                                                             Long course,
                                                                                             Boolean isApproved,
                                                                                             Boolean isProcessed,
                                                                                             String student) {
        return findPage(sessionId, pageNum, results, course, isApproved, isProcessed, student);
    }

    @Override
    public List<CourseEnrollmentWithoutRequirementsStudentRequest> getAllRequestForLoggedInStudent(String index) {
        return requestRepository.findByStudentIndex(index);
    }

    @Override
    public CourseEnrollmentWithoutRequirementsStudentRequest findById(Long id) {
        return requestRepository.findById(id).orElseThrow(() -> new CourseWithoutRequirementRequestNotFoundException(id));
    }

    @Override
    public void create(StudentRequestDto dto) {
        if (dto.getCourse() == null || dto.getDescription().isEmpty()) {
            throw new AllFieldsNotFilledException();
        }

        RequestSession session = requestSessionService.getActiveSessionByType(RequestType.COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS);

        Student student = studentRepository.findById(dto.getStudent())
                .orElseThrow(() -> new StudentNotFoundException(dto.getStudent()));

        String description = dto.getDescription();

        Course course = courseRepository.findById(dto.getCourse())
                .orElseThrow(() -> new CourseNotFoundException(dto.getCourse()));

        CourseEnrollmentWithoutRequirementsStudentRequest request = new CourseEnrollmentWithoutRequirementsStudentRequest();

        request.setStudent(student);
        request.setDescription(description);
        request.setRequestSession(session);
        request.setDateCreated(LocalDate.now());
        request.setCourse(course);
        request.setIsProcessed(false);
        requestRepository.save(request);

    }

    @Override
    public void edit(Long id, StudentRequestDto dto) {
        CourseEnrollmentWithoutRequirementsStudentRequest request = findById(id);

        if (request.getIsApproved() != null) {
            throw new IllegalStateException("Cannot edit a reviewed request");
        }

        if (dto.getCourse() == null || dto.getDescription().isEmpty()) {
            throw new AllFieldsNotFilledException();
        }

        Course course = courseRepository.findById(dto.getCourse())
                .orElseThrow(() -> new CourseNotFoundException(dto.getCourse()));

        request.setCourse(course);
        request.setDescription(dto.getDescription());
        request.setDateCreated(LocalDate.now());
        requestRepository.save(request);
    }

    @Override
    public void delete(Long id) {
        CourseEnrollmentWithoutRequirementsStudentRequest request = findById(id);
        if (request.getIsApproved() != null) {
            throw new IllegalStateException("Cannot delete a reviewed request");
        }
        requestRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Boolean action, String rejectReason) {
        CourseEnrollmentWithoutRequirementsStudentRequest request = findById(id);

        if (action && !request.canBeApproved()) {
            throw new RequestAlreadyProcessedOrAlreadyApprovedException(request);
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
    public void markAsProcessed(Long id) {
        CourseEnrollmentWithoutRequirementsStudentRequest request = findById(id);
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
    public List<CourseEnrollmentWithoutRequirementsStudentRequest> listAllForSession(RequestSession session) {
        return requestRepository.findByRequestSession(session);
    }

    @Override
    public void sendMailsToAAViceDean() {
        List<RequestSession> endedSessions = requestSessionService.getJustEndedSessions();
        List<CourseEnrollmentWithoutRequirementsStudentRequest> cewrStudentRequests = new ArrayList<>();
        for (RequestSession endedSession : endedSessions) {
            cewrStudentRequests.addAll(listAllForSession(endedSession));
        }

        User aaViceDean = userService.findUsersByRole(UserRole.ACADEMIC_AFFAIR_VICE_DEAN).stream().findFirst().orElseThrow();
        String[] to = {aaViceDean.getEmail()};

        for (CourseEnrollmentWithoutRequirementsStudentRequest genStudentRequest : cewrStudentRequests) {
            Map<String, Object> model = new HashMap<>();
            model.put("index", genStudentRequest.getStudent().getIndex());
            emailRepository.sendMailAsync(to, "TestMailToAAViceDean", "email-to-aavicedean-template", null, model, null);
        }
    }

    @Override
    public void sendMailToStudentAdministrationAfterAAViceDeanApproval(CourseEnrollmentWithoutRequirementsStudentRequest request) {
        String[] to = (String[]) userService.findUsersByRole(UserRole.STUDENT_ADMINISTRATION).stream().map(User::getEmail).toArray();
        Map<String, Object> model = new HashMap<>();
        model.put("index", request.getStudent().getIndex());
        model.put("type", request.getRequestSession().getRequestType());
        emailRepository.sendMailAsync(to, "TestMailToStudentAdministration", "email-to-studentadmin-template", null, model, null);
    }

    @Override
    public void sendMailToStudentOnStatusChange(CourseEnrollmentWithoutRequirementsStudentRequest request) {
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

    private Page<CourseEnrollmentWithoutRequirementsStudentRequest> findPage(Long sessionId,
                                                                             Integer pageNum,
                                                                             Integer results,
                                                                             Long course,
                                                                             Boolean isApproved,
                                                                             Boolean isProcessed,
                                                                             String student) {

        Specification<CourseEnrollmentWithoutRequirementsStudentRequest> spec = Specification
                .where(filterEquals(CourseEnrollmentWithoutRequirementsStudentRequest.class, "requestSession.id", sessionId))
                .and(filterEquals(CourseEnrollmentWithoutRequirementsStudentRequest.class, "course", course))
                .and(filterEquals(CourseEnrollmentWithoutRequirementsStudentRequest.class, "isApproved", isApproved))
                .and(filterEquals(CourseEnrollmentWithoutRequirementsStudentRequest.class, "isProcessed", isProcessed))
                .and(filterEquals(CourseEnrollmentWithoutRequirementsStudentRequest.class, "student.index", student));

        return requestRepository.findAll(spec, getPageRequest(pageNum, results));
    }

    private PageRequest getPageRequest(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize, Sort.Direction.ASC, "id");
    }
}
