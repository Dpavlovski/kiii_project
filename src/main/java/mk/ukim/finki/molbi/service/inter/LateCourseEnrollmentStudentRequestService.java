package mk.ukim.finki.molbi.service.inter;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.enums.SemesterType;
import mk.ukim.finki.molbi.model.requests.LateCourseEnrollmentStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LateCourseEnrollmentStudentRequestService {
    @Transactional
    Page<LateCourseEnrollmentStudentRequest> filterAndPaginateRequests
            (Long sessionId, Integer pageNum, Integer results, Boolean professorApproved, Boolean isApproved,
             Boolean isProcessed, String student, String professor);

    @Transactional
    List<LateCourseEnrollmentStudentRequest> getAllRequestForLoggedInStudent(String index);

    Page<LateCourseEnrollmentStudentRequest> getAllRequestsForLoggedInProfessor(String professor, String subjectId,
                                                                                SemesterType semesterType, int page, int size);

    LateCourseEnrollmentStudentRequest findById(Long id);

    void create(StudentRequestDto dto);

    void edit(Long id, StudentRequestDto dto);

    void delete(Long id);

    void processApprovalByProfessor(Long id) throws MessagingException;

    void updateStatus(Long id, Boolean action, String rejectReason) throws MessagingException;

    void markAsProcessed(Long id) throws MessagingException;

    Long totalRequestBySession(Long sessionId);

    List<LateCourseEnrollmentStudentRequest> listAllForSession(RequestSession session);

    void sendMailsToProfessors() throws MessagingException;

    void sendMailToAAViceDeanAfterProfessorApproval(LateCourseEnrollmentStudentRequest request) throws MessagingException;

    void sendMailToStudentAdministrationAfterAAViceDeanApproval(LateCourseEnrollmentStudentRequest request) throws MessagingException;


    void sendMailToStudentOnStatusChange(LateCourseEnrollmentStudentRequest request) throws MessagingException;

}
