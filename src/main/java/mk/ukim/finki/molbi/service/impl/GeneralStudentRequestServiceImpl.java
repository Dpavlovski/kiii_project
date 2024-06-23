package mk.ukim.finki.molbi.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import mk.ukim.finki.molbi.model.base.Student;
import mk.ukim.finki.molbi.model.base.User;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.enums.UserRole;
import mk.ukim.finki.molbi.model.exceptions.*;
import mk.ukim.finki.molbi.model.requests.GeneralStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import mk.ukim.finki.molbi.repository.EmailRepository;
import mk.ukim.finki.molbi.repository.GeneralStudentRequestRepository;
import mk.ukim.finki.molbi.repository.StudentRepository;
import mk.ukim.finki.molbi.service.inter.GeneralStudentRequestService;
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
public class GeneralStudentRequestServiceImpl implements GeneralStudentRequestService {

    private final GeneralStudentRequestRepository requestRepository;
    private final StudentRepository studentRepository;
    private final UserService userService;
    private final EmailRepository emailRepository;
    private final RequestSessionService requestSessionService;

    @Override
    @Transactional
    public Page<GeneralStudentRequest> filterAndPaginateRequests(Long sessionId,
                                                                 Integer pageNum,
                                                                 Integer results,
                                                                 Boolean isApproved,
                                                                 Boolean isProcessed,
                                                                 String student) {

        return findPage(sessionId, pageNum, results, isApproved, isProcessed, student);
    }

    @Override
    public List<GeneralStudentRequest> getAllRequestForLoggedInStudent(String index) {
        return requestRepository.findByStudentIndex(index);
    }

    @Override
    public GeneralStudentRequest findById(Long id) {
        return requestRepository.findById(id).orElseThrow(() -> new GeneralStudentRequestNotFoundException(id));
    }

    @Override
    public void create(StudentRequestDto dto) {
        if (dto.getDescription().isEmpty()) {
            throw new AllFieldsNotFilledException();
        }

        RequestSession session = requestSessionService.getActiveSessionByType(RequestType.GENERAL);
        Student student = studentRepository.findById(dto.getStudent()).orElseThrow(() -> new StudentNotFoundException(dto.getStudent()));

        GeneralStudentRequest request = new GeneralStudentRequest();
        request.setDescription(dto.getDescription());
        request.setStudent(student);
        request.setDateCreated(LocalDate.now());
        request.setRequestSession(session);
        request.setIsProcessed(false);
        requestRepository.save(request);
    }

    @Override
    public void edit(Long id, StudentRequestDto dto) {
        GeneralStudentRequest request = findById(id);

        if (request.getIsApproved() != null) {
            throw new IllegalStateException("Cannot edit a reviewed request");
        }

        if (dto.getDescription().isEmpty()) {
            throw new AllFieldsNotFilledException();
        }

        request.setDescription(dto.getDescription());
        request.setDateCreated(LocalDate.now());
        requestRepository.save(request);
    }

    @Override
    public void delete(Long id) {
        GeneralStudentRequest request = findById(id);

        if (request.getIsApproved() != null) {
            throw new IllegalStateException("Cannot delete a reviewed request");
        }
        requestRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Boolean action, String rejectReason) {
        GeneralStudentRequest request = findById(id);
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
        GeneralStudentRequest request = findById(id);
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
    public List<GeneralStudentRequest> listAllForSession(RequestSession session) {
        return requestRepository.findByRequestSession(session);
    }

    @Override
    public void sendMailsToAAViceDean() {
        List<RequestSession> endedSessions = requestSessionService.getJustEndedSessions();
        List<GeneralStudentRequest> genStudentRequests = new ArrayList<>();
        for (RequestSession endedSession : endedSessions) {
            genStudentRequests.addAll(listAllForSession(endedSession));
        }

        User aaViceDean = userService.findUsersByRole(UserRole.ACADEMIC_AFFAIR_VICE_DEAN).stream().findFirst().orElseThrow();
        String[] to = {aaViceDean.getEmail()};

        for (GeneralStudentRequest genStudentRequest : genStudentRequests) {
            Map<String, Object> model = new HashMap<>();
            model.put("index", genStudentRequest.getStudent().getIndex());
            emailRepository.sendMailAsync(to, "TestMailToAAViceDean", "email-to-aavicedean-template", null, model, null);
        }
    }

    @Override
    public void sendMailToStudentAdministrationAfterAAViceDeanApproval(GeneralStudentRequest request) {
        String[] to = (String[]) userService.findUsersByRole(UserRole.STUDENT_ADMINISTRATION).stream().map(User::getEmail).toArray();
        Map<String, Object> model = new HashMap<>();
        model.put("index", request.getStudent().getIndex());
        model.put("type", request.getRequestSession().getRequestType());
        emailRepository.sendMailAsync(to, "TestMailToStudentAdministration", "email-to-studentadmin-template", null, model, null);
    }

    @Override
    public void sendMailToStudentOnStatusChange(GeneralStudentRequest request) {
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

    private Page<GeneralStudentRequest> findPage(Long sessionId,
                                                 Integer pageNum,
                                                 Integer results,
                                                 Boolean isApproved,
                                                 Boolean isProcessed,
                                                 String student) {


        Specification<GeneralStudentRequest> spec = Specification
                .where(filterEquals(GeneralStudentRequest.class, "requestSession.id", sessionId))
                .and(filterEquals(GeneralStudentRequest.class, "isApproved", isApproved))
                .and(filterEquals(GeneralStudentRequest.class, "isProcessed", isProcessed))
                .and(filterEquals(GeneralStudentRequest.class, "student.index", student));

        return requestRepository.findAll(spec, getPageRequest(pageNum, results));
    }

    private PageRequest getPageRequest(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize, Sort.Direction.ASC, "id");
    }
}
