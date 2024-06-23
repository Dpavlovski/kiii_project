package mk.ukim.finki.molbi.repository;

import mk.ukim.finki.molbi.model.base.Course;
import mk.ukim.finki.molbi.model.base.SemesterState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findBySemesterStateOrderByJoinedSubjectNameAsc(SemesterState state);
}
