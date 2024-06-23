package mk.ukim.finki.molbi.service.inter;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.exceptions.*;
import mk.ukim.finki.molbi.model.requests.ChangeStudyProgramStudentRequest;
import mk.ukim.finki.molbi.model.requests.GeneralStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface ChangeStudyProgramStudentRequestService {

    @Transactional
    Page<ChangeStudyProgramStudentRequest> filterAndPaginateRequests
            (Long sessionId,
             Integer pageNum,
             Integer results,
             String oldStudyProgram,
             String newStudyProgram,
             Boolean isApproved,
             Boolean isProcessed,
             String student);

    @Transactional
    List<ChangeStudyProgramStudentRequest> getAllRequestForLoggedInStudent(String index);

    ChangeStudyProgramStudentRequest findById(Long id);

    void create(StudentRequestDto dto) throws ChangeStudyProgramStudentRequestException;

    void edit(Long id, StudentRequestDto dto);

    void delete(Long id);

    void updateStatus(Long id, Boolean action, String rejectReason) throws MessagingException;

    void markAsProcessed(Long id) throws MessagingException;

    Long totalRequestBySession(Long sessionId);

    List<ChangeStudyProgramStudentRequest> listAllForSession(RequestSession session);

    void sendMailsToAAViceDean() throws MessagingException;

    void sendMailToStudentAdministrationAfterAAViceDeanApproval(ChangeStudyProgramStudentRequest request) throws MessagingException;

    void sendMailToStudentOnStatusChange(ChangeStudyProgramStudentRequest request) throws MessagingException;
}
