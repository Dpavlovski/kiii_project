package mk.ukim.finki.molbi.service.inter;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.requests.CourseGroupChangeStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseGroupChangeStudentRequestService {
    @Transactional
    Page<CourseGroupChangeStudentRequest> filterAndPaginateRequests
            (Long sessionId,
             Integer pageNum,
             Integer results,
             Long course,
             String oldProfessor,
             String newProfessor,
             Boolean isApproved,
             Boolean isProcessed,
             String student);

    @Transactional
    List<CourseGroupChangeStudentRequest> getAllRequestForLoggedInStudent(String index);

    CourseGroupChangeStudentRequest findById(Long id);

    void create(StudentRequestDto dto);

    void edit(Long id, StudentRequestDto dto);

    void delete(Long id);

    void updateStatus(Long id, Boolean action, String rejectReason) throws MessagingException;

    void markAsProcessed(Long id) throws MessagingException;

    Long totalRequestBySession(Long sessionId);

    List<CourseGroupChangeStudentRequest> listAllForSession(RequestSession session);

    void sendMailsToAAViceDean() throws MessagingException;

    void sendMailToStudentAdministrationAfterAAViceDeanApproval(CourseGroupChangeStudentRequest request) throws MessagingException;

    void sendMailToStudentOnStatusChange(CourseGroupChangeStudentRequest request) throws MessagingException;

}
