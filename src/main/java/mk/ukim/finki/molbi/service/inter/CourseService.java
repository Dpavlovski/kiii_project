package mk.ukim.finki.molbi.service.inter;

import mk.ukim.finki.molbi.model.base.Course;

import java.util.List;

public interface CourseService {
    List<Course> listBySemesterState();

    Course findById(Long id);

    void save(Course course);
}
