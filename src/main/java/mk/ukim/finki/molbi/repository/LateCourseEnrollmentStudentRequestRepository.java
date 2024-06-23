package mk.ukim.finki.molbi.repository;

import mk.ukim.finki.molbi.model.requests.LateCourseEnrollmentStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LateCourseEnrollmentStudentRequestRepository extends JpaSpecificationRepository<LateCourseEnrollmentStudentRequest, Long> {
    Long countByRequestSession_Id(Long sessionId);

    List<LateCourseEnrollmentStudentRequest> findByRequestSession(RequestSession requestSession);

    List<LateCourseEnrollmentStudentRequest> findByStudentIndex(String index);

}
