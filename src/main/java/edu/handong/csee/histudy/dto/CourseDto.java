package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseDto {

  @Schema(description = "List of courses", type = "array")
  private List<CourseInfo> courses;

  @AllArgsConstructor
  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class CourseInfo {

    @Schema(description = "Course ID", example = "1", type = "number")
    private Long id;

    @Schema(description = "Course Name", example = "Software Engineering")
    private String name;

    @Schema(description = "Course Professor", example = "Prof. John Doe")
    private String prof;

    @Schema(description = "Course Code", example = "CSEE 4111")
    private String code;

    @Schema(description = "Course Year", example = "2021", type = "number")
    private int year;

    @Schema(description = "Course Semester", example = "1", type = "number")
    private int semester;

    public CourseInfo(Course course) {
      this.id = course.getCourseId();
      this.name = course.getName();
      this.prof = course.getProfessor();
      this.code = course.getCode();
      this.year = course.getAcademicTerm().getAcademicYear();
      this.semester = course.getAcademicTerm().getSemester().getNumber();
    }
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class BasicCourseInfo {

    @Schema(description = "Course ID", example = "1", type = "number")
    private Long id;

    @Schema(description = "Course Name", example = "Software Engineering")
    private String name;

    @Schema(description = "Course Professor", example = "Prof. John Doe")
    private String prof;

    public BasicCourseInfo(Course course) {
      this.id = course.getCourseId();
      this.name = course.getName();
      this.prof = course.getProfessor();
    }
  }
}
