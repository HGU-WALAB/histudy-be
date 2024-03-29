package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.exception.ForbiddenException;
import edu.handong.csee.histudy.repository.AcademicTermRepository;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.CourseService;
import edu.handong.csee.histudy.service.ImageService;
import edu.handong.csee.histudy.service.ReportService;
import edu.handong.csee.histudy.service.TeamService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "스터디 그룹 API")
@SecurityRequirement(name = "MEMBER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
public class TeamController {

  private final ReportService reportService;
  private final CourseService courseService;
  private final TeamService teamService;
  private final ImageService imageService;
  private final UserRepository userRepository;
  private final AcademicTermRepository academicTermRepository;
  private final StudyGroupRepository studyGroupRepository;

  @Operation(summary = "그룹 스터디 보고서 생성")
  @PostMapping("/reports")
  public ReportDto.ReportInfo createReport(
      @RequestBody ReportForm form, @RequestAttribute Claims claims) {
    if (Role.isAuthorized(claims, Role.MEMBER)) {
      return reportService.createReport(form, claims.getSubject());
    }
    throw new ForbiddenException();
  }

  @Operation(summary = "그룹 보고서 목록 조회")
  @GetMapping("/reports")
  public ReportDto getMyGroupReports(@RequestAttribute Claims claims) {
    if (Role.isAuthorized(claims, Role.MEMBER)) {
      List<ReportDto.ReportInfo> reports = reportService.getReports(claims.getSubject());
      return new ReportDto(reports);
    }
    throw new ForbiddenException();
  }

  @Operation(summary = "그룹 특정 보고서 조회")
  @SecurityRequirements({
    @SecurityRequirement(name = "MEMBER"),
    @SecurityRequirement(name = "ADMIN")
  })
  @GetMapping("/reports/{reportId}")
  public ResponseEntity<ReportDto.ReportInfo> getReport(
      @PathVariable Long reportId, @RequestAttribute Claims claims) {
    if (Role.isAuthorized(claims, Role.MEMBER, Role.ADMIN)) {
      Optional<ReportDto.ReportInfo> reportsOr = reportService.getReport(reportId);
      return reportsOr.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    throw new ForbiddenException();
  }

  @Operation(summary = "그룹 특정 보고서 수정")
  @Parameter(name = "reportId", in = ParameterIn.PATH, example = "1")
  @PatchMapping("/reports/{reportId}")
  public ResponseEntity<String> updateReport(
      @PathVariable Long reportId, @RequestBody ReportForm form, @RequestAttribute Claims claims) {
    if (Role.isAuthorized(claims, Role.MEMBER)) {
      return (reportService.updateReport(reportId, form))
          ? ResponseEntity.ok().build()
          : ResponseEntity.notFound().build();
    }
    throw new ForbiddenException();
  }

  @Operation(summary = "그룹 특정 보고서 삭제")
  @Parameter(name = "reportId", in = ParameterIn.PATH, example = "1")
  @DeleteMapping("/reports/{reportId}")
  public ResponseEntity<String> deleteReport(
      @PathVariable Long reportId, @RequestAttribute Claims claims) {
    if (Role.isAuthorized(claims, Role.MEMBER)) {
      return (reportService.deleteReport(reportId))
          ? ResponseEntity.ok().build()
          : ResponseEntity.notFound().build();
    }
    throw new ForbiddenException();
  }

  @Operation(summary = "그룹 선택 강의 목록 조회")
  @GetMapping("/courses")
  public ResponseEntity<CourseDto> getTeamCourses(@RequestAttribute Claims claims) {
    if (Role.isAuthorized(claims, Role.MEMBER)) {
      return ResponseEntity.ok(new CourseDto(courseService.getTeamCourses(claims.getSubject())));
    }
    throw new ForbiddenException();
  }

  @Operation(summary = "그룹 팀원 목록 조회")
  @GetMapping("/users")
  public ResponseEntity<List<UserDto.UserMeWithMasking>> getTeamUsers(
      @RequestAttribute Claims claims) {
    if (Role.isAuthorized(claims, Role.MEMBER)) {
      return ResponseEntity.ok(teamService.getTeamUsers(claims.getSubject()));
    }
    throw new ForbiddenException();
  }

  /**
   * 이미지를 업로드하고, 저장한 이미지 경로를 반환하는 API
   *
   * <p>스터디 보고서를 생성하는 API를 호출하기 전에 호출되어야 한다.
   *
   * @param image 이미지 파일
   * @param claims 토큰 페이로드
   * @return 저장한 이미지 경로
   * @see #createReport(ReportForm, Claims)
   */
  @Operation(summary = "스터디 보고서에 들어갈 인증 이미지 업로드")
  @PostMapping(
      path = {"/reports/image", "/reports/{reportIdOr}/image"},
      consumes = "multipart/form-data")
  @ApiResponse(
      responseCode = "200",
      content =
          @Content(
              mediaType = "application/json",
              examples = @ExampleObject(value = "{\"imagePath\": \"/path/to/image.png\"}")))
  public ResponseEntity<Map<String, String>> uploadImage(
      @PathVariable(required = false) Optional<Long> reportIdOr,
      @RequestParam MultipartFile image,
      @RequestAttribute Claims claims) {
    if (Role.isAuthorized(claims, Role.MEMBER)) {
      String filename = imageService.getImagePaths(claims.getSubject(), image, reportIdOr);
      Map<String, String> response = Map.of("imagePath", filename);
      return ResponseEntity.ok(response);
    }
    throw new ForbiddenException();
  }
}
