package mk.ukim.finki.molbi.service.impl;

import lombok.AllArgsConstructor;
import mk.ukim.finki.molbi.model.base.Semester;
import mk.ukim.finki.molbi.model.dtos.RequestSessionDto;
import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.exceptions.RequestSessionNotFoundException;
import mk.ukim.finki.molbi.model.exceptions.RequestsInRequestSessionException;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import mk.ukim.finki.molbi.repository.*;
import mk.ukim.finki.molbi.service.inter.RequestSessionService;
import mk.ukim.finki.molbi.service.inter.SemesterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static mk.ukim.finki.molbi.service.specification.FieldFilterSpecification.*;

@Service
@AllArgsConstructor
public class RequestSessionServiceImpl implements RequestSessionService {

    private final SemesterService semesterService;
    private final GeneralStudentRequestRepository genRepository;
    private final CourseEnrollmentWithoutRequirementsStudentRequestRepository cewrRepository;
    private final ChangeStudyProgramStudentRequestRepository spcRepository;
    private final CourseGroupChangeStudentRequestRepository cgcRepository;
    private final LateCourseEnrollmentStudentRequestRepository lceRepository;
    private final RequestSessionRepository repository;

    @Override
    public Page<RequestSession> filterAndPaginateSessions(Integer pageNum, Integer results, LocalDateTime timeFrom,
                                                          LocalDateTime timeTo, String semester,
                                                          String requestType, Boolean active) {
        if (active != null && active) {
            timeFrom = LocalDateTime.now();
            timeTo = LocalDateTime.now();
        }

        return findPage(pageNum, results, timeFrom, timeTo, semester, requestType);
    }

    @Override
    public Page<RequestSession> getAllActiveSessions(Integer pageNum, Integer results) {
        return findPage(pageNum, results, LocalDateTime.now(), LocalDateTime.now(), null, null);
    }

    @Override
    public RequestSession getActiveSessionByType(RequestType type) {
        return getAllActiveSessions(1, Integer.MAX_VALUE).stream()
                .filter(s -> s.getRequestType().equals(type)).findFirst()
                .orElseThrow();
    }

    @Override
    public List<RequestSession> getJustEndedSessions() {
        LocalDate now = LocalDate.now().minusDays(1);
        return repository.findAll().stream().filter(s -> s.getTimeTo().toLocalDate().equals(now)).toList();
    }

    @Override
    public RequestSession findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RequestSessionNotFoundException(id));
    }

    @Override
    public void save(Long id, RequestSessionDto dto) {
        RequestSession requestSession;
        if (id != null) {
            requestSession = this.findById(id);
        } else {
            requestSession = new RequestSession();
        }

        requestSession.setDescription(dto.getDescription());
        requestSession.setRequestType(dto.getRequestType());

        Semester semester = semesterService.findById(dto.getSemester());
        requestSession.setSemester(semester);

        requestSession.setTimeFrom(dto.getTimeFrom());
        requestSession.setTimeTo(dto.getTimeTo());
        requestSession.setApprovalNote(dto.getApprovalNote());
        repository.save(requestSession);
    }

    @Override
    public void delete(Long id) {
        RequestSession session = findById(id);
        if (session.getRequestType().equals(RequestType.LATE_COURSE_ENROLLMENT))
            if (lceRepository.countByRequestSession_Id(id) > 0) {
                throw new RequestsInRequestSessionException(id);
            } else {
                repository.deleteById(id);
                return;
            }
        else if (session.getRequestType().equals(RequestType.GENERAL))
            if (genRepository.countByRequestSession_Id(id) > 0) {
                throw new RequestsInRequestSessionException(id);
            } else {
                repository.deleteById(id);
            }
        else if (session.getRequestType().equals(RequestType.STUDY_PROGRAM_CHANGE))
            if (spcRepository.countByRequestSession_Id(id) > 0) {
                throw new RequestsInRequestSessionException(id);
            } else {
                repository.deleteById(id);
            }
        else if (session.getRequestType().equals(RequestType.COURSE_GROUP_CHANGE))
            if (cgcRepository.countByRequestSession_Id(id) > 0) {
                throw new RequestsInRequestSessionException(id);
            } else {
                repository.deleteById(id);
            }
        else if (session.getRequestType().equals(RequestType.COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS))
            if (cewrRepository.countByRequestSession_Id(id) > 0) {
                throw new RequestsInRequestSessionException(id);
            } else {
                repository.deleteById(id);
            }
    }

    private Page<RequestSession> findPage(Integer pageNum, Integer results, LocalDateTime timeFrom,
                                          LocalDateTime timeTo, String semester,
                                          String requestType) {
        Specification<RequestSession> spec = Specification
                .where(filterIsAfter(RequestSession.class, "timeFrom", timeFrom))
                .and(filterIsBefore(RequestSession.class, "timeTo", timeTo))
                .and(filterEquals(RequestSession.class, "semester.code", semester));

        if (requestType != null && !requestType.isEmpty()) {
            spec = spec.and(filterEquals(RequestSession.class, "requestType", RequestType.valueOf(requestType)));
        }

        return repository.findAll(spec, getPageRequest(pageNum, results));
    }

    private PageRequest getPageRequest(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize, Sort.Direction.ASC, "id");
    }
}
