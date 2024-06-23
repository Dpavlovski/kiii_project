package mk.ukim.finki.molbi.model.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@MappedSuperclass
@Getter
@Setter
public class TrackedHistoryEntity {

    @JsonIgnore
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JSR310Module());
    }

    @JsonIgnore
    @ElementCollection
    private List<String> historyJson = new ArrayList<>();

    private LocalDateTime lastUpdateTime;

    private String lastUpdateUser;

    @PreUpdate
    @PrePersist
    public void addHistory() {
        this.lastUpdateTime = LocalDateTime.now();
        if (SecurityContextHolder.getContext() != null &&
            SecurityContextHolder.getContext().getAuthentication() != null) {
            this.lastUpdateUser = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        try {
            String serializedState = objectMapper.writeValueAsString(this);
            historyJson.add(serializedState);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @JsonIgnore
    public List<? extends TrackedHistoryEntity> getHistory() {
        List<? extends TrackedHistoryEntity> result = this.historyJson.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, this.getClass());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(TrackedHistoryEntity::getLastUpdateTime).reversed())
                .collect(Collectors.toList());
        return result;
    }
}

