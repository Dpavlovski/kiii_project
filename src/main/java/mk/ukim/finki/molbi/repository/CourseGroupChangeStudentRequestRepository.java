package mk.ukim.finki.molbi.repository;

import mk.ukim.finki.molbi.model.requests.CourseGroupChangeStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CourseGroupChangeStudentRequestRepository extends JpaSpecificationRepository<CourseGroupChangeStudentRequest, Long> {
    Long countByRequestSession_Id(Long sessionId);

    List<CourseGroupChangeStudentRequest> findByRequestSession(RequestSession requestSession);

    List<CourseGroupChangeStudentRequest> findByStudentIndex(String index);
}




