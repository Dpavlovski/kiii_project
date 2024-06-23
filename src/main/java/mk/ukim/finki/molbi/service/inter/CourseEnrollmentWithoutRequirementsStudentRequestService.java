package mk.ukim.finki.molbi.service.inter;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.requests.CourseEnrollmentWithoutRequirementsStudentRequest;
import mk.ukim.finki.molbi.model.requests.GeneralStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseEnrollmentWithoutRequirementsStudentRequestService {

    @Transactional
    Page<CourseEnrollmentWithoutRequirementsStudentRequest> filterAndPaginateRequests
            (Long sessionId,
             int pageNum,
             int results,
             Long course,
             Boolean isApproved,
             Boolean isProcessed,
             String student);

    @Transactional
    List<CourseEnrollmentWithoutRequirementsStudentRequest> getAllRequestForLoggedInStudent(String index);

    CourseEnrollmentWithoutRequirementsStudentRequest findById(Long id);

    void create(StudentRequestDto dto);

    void edit(Long id, StudentRequestDto dto);

    void delete(Long id);

    void updateStatus(Long id, Boolean action, String rejectReason) throws MessagingException;

    void markAsProcessed(Long id) throws MessagingException;

    Long totalRequestBySession(Long sessionId);

    List<CourseEnrollmentWithoutRequirementsStudentRequest> listAllForSession(RequestSession session);

    void sendMailsToAAViceDean() throws MessagingException;

    void sendMailToStudentAdministrationAfterAAViceDeanApproval(CourseEnrollmentWithoutRequirementsStudentRequest request) throws MessagingException;

    void sendMailToStudentOnStatusChange(CourseEnrollmentWithoutRequirementsStudentRequest request) throws MessagingException;
}
