package edu.handong.csee.histudy.report;

import edu.handong.csee.histudy.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("dev")
@Transactional
public class ManageReportTests {

    @DisplayName("보고서 작성시 참여한 인원을 선택할 수 있다.")
    @Test
    public void ManageReportTests_16() {
        // given
        User user1 = User.builder()
                .sid("22300000")
                .email("test@histudy.com")
                .name("user1")
                .role(Role.USER)
                .build();
        User user2 = User.builder()
                .sid("22300000")
                .email("test@histudy.com")
                .name("user2")
                .role(Role.USER)
                .build();
        Course course = Course.builder()
                .name("courseName")
                .build();

        Team team = new Team(1);
        Report report = Report.builder()
                .title("title")
                .content("content")
                .totalMinutes(60)
                .team(team)
                .participants(List.of(user1))
                .courses(List.of(course))
                .build();
        user1.belongTo(team);
        user2.belongTo(team);

        // then
        assertEquals(2, report.getTeam().getUsers().size());
        assertEquals(1, report.getParticipants().size());
    }

    @DisplayName("보고서 작성시 활동 시간을 저장할 수 있다.")
    @Test
    public void ManageReportTests_61() {
        // given
        Course course = Course.builder()
                .name("courseName")
                .build();
        Report report = Report.builder()
                .title("title")
                .content("content")
                .totalMinutes(60)
                .team(new Team(1))
                .participants(List.of(User.builder().build()))
                .courses(List.of(course))
                .build();

        // then
        assertEquals(60, report.getTotalMinutes());
    }

    @DisplayName("활동 시간은 그룹 전체 활동 시간에도 집계되어야 한다.")
    @Test
    public void ManageReportTests_76() {
        // given
        Course course = Course.builder()
                .name("courseName")
                .build();
        Team team = new Team(1);

        Report report1 = Report.builder()
                .title("title")
                .content("content")
                .totalMinutes(30)
                .team(team)
                .participants(List.of(User.builder().build()))
                .courses(List.of(course))
                .build();
        Report report2 = Report.builder()
                .title("title")
                .content("content")
                .totalMinutes(60)
                .team(team)
                .participants(List.of(User.builder().build()))
                .courses(List.of(course))
                .build();
        // then
        assertEquals(90, team.getTotalMinutes());
    }

    @DisplayName("이미지를 1개 이상 첨부할 수 있다.")
    @Test
    public void ManageReportTests_102() {
        // given
        Course course = Course.builder()
                .name("courseName")
                .build();
        Report report = Report.builder()
                .title("title")
                .content("content")
                .totalMinutes(0)
                .team(new Team(1))
                .participants(List.of(User.builder().build()))
                .images(List.of("pathA", "pathB", "pathC"))
                .courses(List.of(course))
                .build();

        // then
        assertEquals(3, report.getImages().size());
        assertEquals(Collections.nCopies(3, report), report.getImages()
                .stream()
                .map(Image::getReport)
                .toList());
    }

    @DisplayName("이미지의 순서는 보장되어야 한다.")
    @Test
    public void ManageReportTests_122() {
        // given
        Course course = Course.builder()
                .name("courseName")
                .build();

        Report report = Report.builder()
                .title("title")
                .content("content")
                .totalMinutes(0)
                .team(new Team(1))
                .participants(List.of(User.builder().build()))
                .images(List.of("pathA", "pathB", "pathC"))
                .courses(List.of(course))
                .build();

        // then
        assertEquals(3, report.getImages().size());
        assertEquals(List.of("pathA", "pathB", "pathC"),
                report.getImages()
                        .stream()
                        .map(Image::getPath)
                        .toList());
    }
}
