package mk.ukim.finki.molbi.service.impl;


import lombok.AllArgsConstructor;
import mk.ukim.finki.molbi.model.base.Student;
import mk.ukim.finki.molbi.model.base.StudyProgram;
import mk.ukim.finki.molbi.model.base.User;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.enums.UserRole;
import mk.ukim.finki.molbi.model.exceptions.*;
import mk.ukim.finki.molbi.model.requests.ChangeStudyProgramStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import mk.ukim.finki.molbi.repository.ChangeStudyProgramStudentRequestRepository;
import mk.ukim.finki.molbi.repository.EmailRepository;
import mk.ukim.finki.molbi.repository.StudentRepository;
import mk.ukim.finki.molbi.repository.StudyProgramRepository;
import mk.ukim.finki.molbi.service.inter.ChangeStudyProgramStudentRequestService;
import mk.ukim.finki.molbi.service.inter.RequestSessionService;
import mk.ukim.finki.molbi.service.inter.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mk.ukim.finki.molbi.service.specification.FieldFilterSpecification.filterEquals;


@AllArgsConstructor
@Service
public class ChangeStudyProgramStudentRequestServiceImpl implements ChangeStudyProgramStudentRequestService {

    private final ChangeStudyProgramStudentRequestRepository requestRepository;
    private final StudyProgramRepository studyProgramRepository;
    private final StudentRepository studentRepository;
    private final RequestSessionService requestSessionService;
    private final UserService userService;
    private final EmailRepository emailRepository;

    @Override
    @Transactional
    public Page<ChangeStudyProgramStudentRequest> filterAndPaginateRequests(Long sessionId,
                                                                            Integer pageNum,
                                                                            Integer results,
                                                                            String oldStudyProgram,
                                                                            String newStudyProgram,
                                                                            Boolean isApproved,
                                                                            Boolean isProcessed,
                                                                            String student) {
        return findPage(sessionId, pageNum, results, oldStudyProgram, newStudyProgram, isApproved, isProcessed, student);
    }

    @Override
    @Transactional
    public List<ChangeStudyProgramStudentRequest> getAllRequestForLoggedInStudent(String index) {
        return requestRepository.findByStudentIndex(index);
    }

    @Override
    public ChangeStudyProgramStudentRequest findById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ChangeStudyProgramStudentRequestNotFoundException(id));
    }

    @Override
    @Transactional
    public void create(StudentRequestDto dto) throws StudentNotFoundException, ChangeStudyProgramStudentRequestException {

        if (dto.getNewStudyProgram().isEmpty() || dto.getDescription().isEmpty()) {
            throw new AllFieldsNotFilledException();
        }

        RequestSession session = requestSessionService.getActiveSessionByType(RequestType.STUDY_PROGRAM_CHANGE);

        Student student = studentRepository.findById(dto.getStudent())
                .orElseThrow(() -> new StudentNotFoundException(dto.getStudent()));

        StudyProgram oldStudyProgram = student.getStudyProgram();
        StudyProgram newStudyProgram = this.studyProgramRepository.findById(dto.getNewStudyProgram())
                .orElseThrow(() -> new ChangeStudyProgramStudentRequestException(dto.getNewStudyProgram()));

        if (oldStudyProgram.getName().equals(newStudyProgram.getName())) {
            throw new SameStudyProgramsException();
        }

        ChangeStudyProgramStudentRequest request = new ChangeStudyProgramStudentRequest();

        request.setOldStudyProgram(oldStudyProgram);
        request.setNewStudyProgram(newStudyProgram);
        request.setStudent(student);
        request.setDescription(dto.getDescription());
        request.setRequestSession(session);
        request.setDateCreated(LocalDate.now());
        request.setIsProcessed(false);
        requestRepository.save(request);
    }

    @Override
    public void edit(Long id, StudentRequestDto dto) {
        ChangeStudyProgramStudentRequest request = findById(id);

        if (request.getIsApproved() != null) {
            throw new IllegalStateException("Cannot edit a reviewed request");
        }

        if (dto.getNewStudyProgram() == null || dto.getDescription().isEmpty()) {
            throw new AllFieldsNotFilledException();
        }

        StudyProgram oldStudyProgram = request.getStudent().getStudyProgram();

        StudyProgram newStudyProgram = studyProgramRepository.findById(dto.getNewStudyProgram())
                .orElseThrow(() -> new ChangeStudyProgramStudentRequestException(dto.getNewStudyProgram()));

        if (oldStudyProgram.getName().equals(newStudyProgram.getName())) {
            throw new SameStudyProgramsException();
        }

        request.setOldStudyProgram(oldStudyProgram);
        request.setNewStudyProgram(newStudyProgram);
        request.setDescription(dto.getDescription());
        request.setDateCreated(LocalDate.now());
        requestRepository.save(request);
    }

    @Override
    public void delete(Long id) {
        ChangeStudyProgramStudentRequest request = findById(id);
        if (request.getIsApproved() != null) {
            throw new IllegalStateException("Cannot delete a reviewed request");
        }
        requestRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Boolean action, String rejectReason) {
        ChangeStudyProgramStudentRequest request = this.findById(id);
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
    public void markAsProcessed(Long id) throws ChangeStudyProgramStudentRequestNotFoundException {
        ChangeStudyProgramStudentRequest request = this.findById(id);
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
    public List<ChangeStudyProgramStudentRequest> listAllForSession(RequestSession session) {
        return requestRepository.findByRequestSession(session);
    }

    @Override
    public void sendMailsToAAViceDean() {
        List<RequestSession> endedSessions = requestSessionService.getJustEndedSessions();
        List<ChangeStudyProgramStudentRequest> spcStudentRequests = new ArrayList<>();
        for (RequestSession endedSession : endedSessions) {
            spcStudentRequests.addAll(listAllForSession(endedSession));
        }

        User aaViceDean = userService.findUsersByRole(UserRole.ACADEMIC_AFFAIR_VICE_DEAN).stream().findFirst().orElseThrow();
        String[] to = {aaViceDean.getEmail()};

        for (ChangeStudyProgramStudentRequest spcStudentRequest : spcStudentRequests) {
            Map<String, Object> model = new HashMap<>();
            model.put("index", spcStudentRequest.getStudent().getIndex());
            emailRepository.sendMailAsync(to, "TestMailToAAViceDean", "email-to-aavicedean-template", null, model, null);
        }
    }

    @Override
    public void sendMailToStudentAdministrationAfterAAViceDeanApproval(ChangeStudyProgramStudentRequest request) {
        String[] to = (String[]) userService.findUsersByRole(UserRole.STUDENT_ADMINISTRATION).stream().map(User::getEmail).toArray();
        Map<String, Object> model = new HashMap<>();
        model.put("index", request.getStudent().getIndex());
        model.put("type", request.getRequestSession().getRequestType());
        emailRepository.sendMailAsync(to, "TestMailToStudentAdministration", "email-to-studentadmin-template", null, model, null);
    }

    @Override
    public void sendMailToStudentOnStatusChange(ChangeStudyProgramStudentRequest request) {
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

    private Page<ChangeStudyProgramStudentRequest> findPage(Long sessionId,
                                                            Integer pageNum,
                                                            Integer results,
                                                            String oldStudyProgram,
                                                            String newStudyProgram,
                                                            Boolean isApproved,
                                                            Boolean isProcessed,
                                                            String student) {


        Specification<ChangeStudyProgramStudentRequest> spec = Specification
                .where(filterEquals(ChangeStudyProgramStudentRequest.class, "requestSession.id", sessionId))
                .and(filterEquals(ChangeStudyProgramStudentRequest.class, "oldStudyProgram", oldStudyProgram))
                .and(filterEquals(ChangeStudyProgramStudentRequest.class, "newStudyProgram", newStudyProgram))
                .and(filterEquals(ChangeStudyProgramStudentRequest.class, "isApproved", isApproved))
                .and(filterEquals(ChangeStudyProgramStudentRequest.class, "isProcessed", isProcessed))
                .and(filterEquals(ChangeStudyProgramStudentRequest.class, "student.index", student));

        return requestRepository.findAll(spec, getPageRequest(pageNum, results));
    }

    private PageRequest getPageRequest(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize, Sort.Direction.ASC, "id");
    }
}
