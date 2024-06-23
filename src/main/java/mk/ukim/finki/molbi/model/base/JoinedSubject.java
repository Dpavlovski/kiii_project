package mk.ukim.finki.molbi.model.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mk.ukim.finki.molbi.model.enums.SemesterType;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class JoinedSubject extends TrackedHistoryEntity {

    @Id
    private String abbreviation;

    @Column(length = 1000)
    private String name;

    private String codes;

    @Enumerated(EnumType.STRING)
    private SemesterType semesterType;

    @ManyToOne
    private Subject mainSubject;


    private Integer weeklyLecturesClasses;

    private Integer weeklyAuditoriumClasses;

    private Integer weeklyLabClasses;

    @Transient
    public List<String> codesList() {
        return codes != null ? List.of(codes.split(";")).stream().map(String::trim).toList() : List.of();
    }

    @Transient
    public String displayName() {
        return String.format("%s [%s] (%d+%d+%d)", name, abbreviation, weeklyLecturesClasses, weeklyAuditoriumClasses, weeklyLabClasses);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        JoinedSubject other = (JoinedSubject) o;
        return getAbbreviation() != null && Objects.equals(getAbbreviation(), other.getAbbreviation());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

