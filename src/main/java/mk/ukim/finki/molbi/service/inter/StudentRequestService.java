package mk.ukim.finki.molbi.service.inter;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import mk.ukim.finki.molbi.model.dtos.StudentRequestDto;
import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import mk.ukim.finki.molbi.model.requests.StudentRequest;
import org.springframework.data.domain.Page;


public interface StudentRequestService {

    @Transactional
    Page<StudentRequest> getAllRequestsByStudentIndex(String index, int pageNum, int pageSize, RequestType requestType);

    @Transactional
    StudentRequest getRequestById(String index, Long id);

    void create(RequestType requestType, String studentIndex, StudentRequestDto dto);

    void edit(String studentIndex, StudentRequestDto dto, Long id);

    @Transactional
    void deleteRequest(Long id, String index);

    void updateStatus(RequestSession session, Long id, Boolean action, String rejectReason) throws MessagingException;

    void markAsProcessed(RequestSession requestSession, Long id) throws MessagingException;

}
