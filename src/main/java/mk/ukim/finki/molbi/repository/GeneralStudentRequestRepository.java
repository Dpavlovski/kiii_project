package mk.ukim.finki.molbi.repository;

import mk.ukim.finki.molbi.model.requests.GeneralStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface GeneralStudentRequestRepository extends JpaSpecificationRepository<GeneralStudentRequest, Long> {
    Long countByRequestSession_Id(Long sessionId);

    List<GeneralStudentRequest> findByRequestSession(RequestSession requestSession);

    List<GeneralStudentRequest> findByStudentIndex(String index);
}