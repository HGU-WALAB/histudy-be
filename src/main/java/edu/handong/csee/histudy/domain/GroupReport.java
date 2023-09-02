package edu.handong.csee.histudy.domain;

import edu.handong.csee.histudy.controller.form.ReportForm;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GroupReport extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private long totalMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    private StudyGroup studyGroup;

    @OneToMany(mappedBy = "groupReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportUser> participants = new ArrayList<>();

    @OneToMany(mappedBy = "groupReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "groupReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportCourse> courses = new ArrayList<>();

    @Builder
    public GroupReport(String title,
                       String content,
                       long totalMinutes,
                       StudyGroup studyGroup,
                       List<User> participants,
                       List<String> images,
                       List<Course> courses) {
        this.title = title;
        this.content = content;
        this.totalMinutes = totalMinutes;

        this.writtenBy(studyGroup);
        this.add(participants);
        this.insert(images);
        this.study(courses);
        studyGroup.increase(totalMinutes);
    }

    private void study(List<Course> _courses) {
        if (!courses.isEmpty()) {
            courses.clear();
        }
        List<ReportCourse> reportCourses = _courses.stream()
                .map(course -> new ReportCourse(this, course))
                .toList();
        this.courses.addAll(reportCourses);
    }

    private void add(List<User> users) {
        if (!participants.isEmpty()) {
            participants.clear();
        }
        users
                .forEach(user -> {
                    ReportUser reportUser = new ReportUser(user, this);
                    this.participants.add(reportUser);
                    user.getReportParticipation().add(reportUser);
                });
    }

    private void writtenBy(StudyGroup studyGroup) {
        this.studyGroup = studyGroup;
        studyGroup.getReports().add(this);
    }

    private void insert(List<String> images) {
        if (images == null) {
            return;
        } else if (!images.isEmpty()) {
            this.images.clear();
        }
        List<Image> paths = images.stream()
                .map(img -> new Image(img, this))
                .toList();
        this.images.addAll(paths);
    }

    public boolean update(ReportForm form, List<User> participants, List<Course> courses) {
        this.title = requireNonNullElse(form.getTitle(), this.title);
        this.content = requireNonNullElse(form.getContent(), this.content);
        this.totalMinutes = requireNonNullElse(form.getTotalMinutes(), this.totalMinutes);

        this.add(participants);
        this.insert(form.getImages());
        this.study(courses);
        studyGroup.update(totalMinutes, this.totalMinutes);

        return true;
    }
}