package mk.ukim.finki.molbi.service.impl;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.exceptions.NotAuthorizedForThisActionException;
import mk.ukim.finki.molbi.model.exceptions.SessionStillActiveException;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import mk.ukim.finki.molbi.model.requests.StudentRequest;
import mk.ukim.finki.molbi.service.inter.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentRequestServiceImpl implements StudentRequestService {

    private final LateCourseEnrollmentStudentRequestService lceService;
    private final CourseGroupChangeStudentRequestService cgcService;
    private final CourseEnrollmentWithoutRequirementsStudentRequestService cwrService;
    private final ChangeStudyProgramStudentRequestService cspService;
    private final GeneralStudentRequestService genService;
    private final RequestSessionService requestSessionService;

    @Override
    @Transactional
    public Page<StudentRequest> getAllRequestsByStudentIndex(String index, int page, int size, RequestType requestType) {
        List<StudentRequest> allRequests = new ArrayList<>();
        allRequests.addAll(lceService.getAllRequestForLoggedInStudent(index));
        allRequests.addAll(genService.getAllRequestForLoggedInStudent(index));
        allRequests.addAll(cgcService.getAllRequestForLoggedInStudent(index));
        allRequests.addAll(cspService.getAllRequestForLoggedInStudent(index));
        allRequests.addAll(cwrService.getAllRequestForLoggedInStudent(index));

        allRequests.sort(Comparator.comparing(StudentRequest::getDateCreated)
                .thenComparing(StudentRequest::getId).reversed());

        if (requestType != null) {
            allRequests = allRequests.stream()
                    .filter(r -> r.getRequestSession().getRequestType().equals(requestType))
                    .toList();
        }

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        int start = Math.min((int) pageRequest.getOffset(), allRequests.size());
        int end = Math.min((start + pageRequest.getPageSize()), allRequests.size());

        List<StudentRequest> paginatedList = allRequests.subList(start, end);

        return new PageImpl<>(paginatedList, pageRequest, allRequests.size());
    }

    @Override
    @Transactional
    public StudentRequest getRequestById(String index, Long id) {
        Page<StudentRequest> requests = getAllRequestsByStudentIndex(index, 1, Integer.MAX_VALUE, null);
        StudentRequest request = requests.stream().filter(r -> r.getId().equals(id)).findFirst()
                .orElseThrow(NotAuthorizedForThisActionException::new);
        RequestType requestType = request.getRequestSession().getRequestType();
        return switch (requestType) {
            case LATE_COURSE_ENROLLMENT -> lceService.findById(id);
            case COURSE_GROUP_CHANGE -> cgcService.findById(id);
            case COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS -> cwrService.findById(id);
            //todo:case INSTALLMENT_PAYMENT ->  return ipService.findById(id);
            case STUDY_PROGRAM_CHANGE -> cspService.findById(id);
            default -> genService.findById(id);
        };
    }

    @Override
    public void create(RequestType requestType, String studentIndex, StudentRequestDto dto) {
        dto.setStudent(studentIndex);
        switch (requestType) {
            case LATE_COURSE_ENROLLMENT -> lceService.create(dto);
            case COURSE_GROUP_CHANGE -> cgcService.create(dto);
            case STUDY_PROGRAM_CHANGE -> cspService.create(dto);
            case COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS -> cwrService.create(dto);
            //todo: case INSTALLMENT_PAYMENT -> ipService.create(dto);
            default -> genService.create(dto);
        }
    }

    @Override
    @Transactional
    public void edit(String studentIndex, StudentRequestDto dto, Long id) {
        StudentRequest request = getRequestById(studentIndex, id);
        RequestType requestType = request.getRequestSession().getRequestType();
        switch (requestType) {
            case LATE_COURSE_ENROLLMENT -> lceService.edit(id, dto);
            case COURSE_GROUP_CHANGE -> cgcService.edit(id, dto);
            case STUDY_PROGRAM_CHANGE -> cspService.edit(id, dto);
            case COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS -> cwrService.edit(id, dto);
            //todo: case INSTALLMENT_PAYMENT -> ipService.edit(id, dto);
            default -> genService.edit(id, dto);
        }
    }


    @Override
    @Transactional
    public void deleteRequest(Long id, String studentIndex) {
        StudentRequest request = getRequestById(studentIndex, id);
        RequestType requestType = request.getRequestSession().getRequestType();
        switch (requestType) {
            case LATE_COURSE_ENROLLMENT -> lceService.delete(id);
            case COURSE_GROUP_CHANGE -> cgcService.delete(id);
            case STUDY_PROGRAM_CHANGE -> cspService.delete(id);
            case COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS -> cwrService.delete(id);
            //todo: case INSTALLMENT_PAYMENT -> ipService.delete(id);
            default -> genService.delete(id);
        }
    }

    @Override
    public void updateStatus(RequestSession session, Long id, Boolean action, String rejectReason) throws
            MessagingException {
        if (requestSessionService.getAllActiveSessions(1, Integer.MAX_VALUE).stream()
                .anyMatch(s -> s.getId().equals(session.getId()) && !s.getRequestType().equals(RequestType.GENERAL))) {
            throw new SessionStillActiveException(session.getId());
        }
        switch (session.getRequestType()) {
            case LATE_COURSE_ENROLLMENT -> lceService.updateStatus(id, action, rejectReason);
            case COURSE_GROUP_CHANGE -> cgcService.updateStatus(id, action, rejectReason);
            case STUDY_PROGRAM_CHANGE -> cspService.updateStatus(id, action, rejectReason);
            case COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS -> cwrService.updateStatus(id, action, rejectReason);
            //todo: case INSTALLMENT_PAYMENT -> ipService.updateStatus(id, action, rejectReason);
            default -> genService.updateStatus(id, action, rejectReason);
        }
    }

    @Override
    public void markAsProcessed(RequestSession session, Long id) throws MessagingException {
        if (requestSessionService.getAllActiveSessions(1, Integer.MAX_VALUE).stream().anyMatch(s -> s.getId().equals(session.getId()))) {
            throw new SessionStillActiveException(session.getId());
        }
        switch (session.getRequestType()) {
            case LATE_COURSE_ENROLLMENT -> lceService.markAsProcessed(id);
            case COURSE_GROUP_CHANGE -> cgcService.markAsProcessed(id);
            case STUDY_PROGRAM_CHANGE -> cspService.markAsProcessed(id);
            case COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS -> cwrService.markAsProcessed(id);
            //todo: case INSTALLMENT_PAYMENT -> ipService.markAsProcessed(id);
            default -> genService.markAsProcessed(id);
        }
    }
}
