package mk.ukim.finki.molbi.repository;

import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.requests.InstallmentPaymentStudentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InstallmentPaymentRepository extends JpaRepository<InstallmentPaymentStudentRequest, Long> {

    Page<InstallmentPaymentStudentRequest> findByRequestSession_Id(Long requestSessionId, Pageable pageable);

    //todo: replace this with specification like in LateCourseEnrollmentStudentRequestServiceImpl
    @Query("SELECT req FROM InstallmentPaymentStudentRequest req " +
           "WHERE (req.requestSession.id = :sessionId) " +
           "AND (:approved IS NULL OR req.isApproved = :approved) " +
           "AND (:processed IS NULL OR req.isProcessed = :processed) " +
           "AND (:student IS NULL OR req.student.index LIKE :student)" +
           "AND (:requestType IS NULL OR req.requestSession.requestType = :requestType) ")
    Page<InstallmentPaymentStudentRequest> findAllFiltered(
            @Param("sessionId") Long sessionId,
            @Param("approved") Boolean approved,
            @Param("processed") Boolean processed,
            @Param("student") String student,
            @Param("requestType") RequestType requestType,
            Pageable pageable
    );
}