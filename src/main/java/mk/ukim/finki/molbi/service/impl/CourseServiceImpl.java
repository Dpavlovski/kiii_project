package mk.ukim.finki.molbi.service.impl;

import lombok.AllArgsConstructor;
import mk.ukim.finki.molbi.model.base.Course;
import mk.ukim.finki.molbi.model.base.Semester;
import mk.ukim.finki.molbi.model.base.SemesterState;
import mk.ukim.finki.molbi.model.exceptions.CourseNotFoundException;
import mk.ukim.finki.molbi.repository.CourseRepository;
import mk.ukim.finki.molbi.service.inter.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public List<Course> listBySemesterState() {
        return this.courseRepository.findBySemesterStateOrderByJoinedSubjectNameAsc(SemesterState.STARTED);
    }

    @Override
    public Course findById(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Override
    public void save(Course course) {
        courseRepository.save(course);
    }
}
