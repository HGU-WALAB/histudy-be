package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

  List<Course> findAllByNameContainingIgnoreCase(String keyword);

  List<Course> findAllByAcademicTermIsCurrentTrue();
}
