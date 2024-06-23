package mk.ukim.finki.molbi.service.inter;


import jakarta.transaction.Transactional;
import mk.ukim.finki.molbi.model.dtos.RequestSessionDto;
import mk.ukim.finki.molbi.model.enums.RequestType;
import mk.ukim.finki.molbi.model.requests.RequestSession;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestSessionService {
    @Transactional
    Page<RequestSession> filterAndPaginateSessions(Integer pageNum, Integer results, LocalDateTime timeFrom,
                                                   LocalDateTime timeTo, String semester, String requestType,
                                                   Boolean active);

    Page<RequestSession> getAllActiveSessions(Integer pageNum, Integer results);

    RequestSession getActiveSessionByType(RequestType type);

    List<RequestSession> getJustEndedSessions();

    RequestSession findById(Long id);

    void save(Long id, RequestSessionDto dto);

    void delete(Long id);
}
