package mk.ukim.finki.molbi.repository;

import mk.ukim.finki.molbi.model.requests.ChangeStudyProgramStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeStudyProgramStudentRequestRepository extends JpaSpecificationRepository<ChangeStudyProgramStudentRequest, Long> {
    Long countByRequestSession_Id(Long sessionId);

    List<ChangeStudyProgramStudentRequest> findByRequestSession(RequestSession requestSession);

    List<ChangeStudyProgramStudentRequest> findByStudentIndex(String index);
}
