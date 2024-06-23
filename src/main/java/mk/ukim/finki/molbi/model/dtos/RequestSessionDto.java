package mk.ukim.finki.molbi.model.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import mk.ukim.finki.molbi.model.enums.RequestType;

import java.time.LocalDateTime;

@Getter
@Setter
public class RequestSessionDto {
    private LocalDateTime timeFrom;

    private LocalDateTime timeTo;

    @ManyToOne
    private String semester;

    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(length = 5000)
    private String description;

    @Column(length = 5000)
    private String approvalNote;
}
