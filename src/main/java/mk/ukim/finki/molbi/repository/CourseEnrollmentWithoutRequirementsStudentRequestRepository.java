package mk.ukim.finki.molbi.repository;

import mk.ukim.finki.molbi.model.requests.CourseEnrollmentWithoutRequirementsStudentRequest;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseEnrollmentWithoutRequirementsStudentRequestRepository extends
        JpaSpecificationRepository<CourseEnrollmentWithoutRequirementsStudentRequest, Long> {
    Long countByRequestSession_Id(Long sessionId);

    List<CourseEnrollmentWithoutRequirementsStudentRequest> findByRequestSession(RequestSession requestSession);

    List<CourseEnrollmentWithoutRequirementsStudentRequest> findByStudentIndex(String index);
}
