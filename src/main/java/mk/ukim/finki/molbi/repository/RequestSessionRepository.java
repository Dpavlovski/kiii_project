package mk.ukim.finki.molbi.repository;

import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestSessionRepository extends JpaSpecificationRepository<RequestSession, Long> {

    @Query("SELECT CASE WHEN COUNT(req) > 0 THEN true ELSE false END FROM LateCourseEnrollmentStudentRequest req WHERE req.requestSession.id = :reqId")
    Page<Boolean> existsLateCourseEnrollmentStudentRequestByReqId(@Param("reqId") Long reqId, Pageable pageable);

}
