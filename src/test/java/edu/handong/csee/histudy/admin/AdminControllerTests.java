package edu.handong.csee.histudy.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.AdminController;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminControllerTests {

    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    AdminController controller;

    @MockBean
    AuthenticationInterceptor interceptor;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CourseRepository courseRepository;

    @BeforeEach
    void init() throws IOException {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .addInterceptors(interceptor)
                .build();
        when(interceptor.preHandle(any(), any(), any())).thenReturn(true);
    }


    @DisplayName("유저의 신청폼을 삭제한다.")
    @Test
    void UserControllerTests_212() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .sid("1234")
                .role(Role.USER)
                .email("조용히해라")
                .name("한시온")
                .build());
        User friend = userRepository.save(User.builder()
                .sid("12321")
                .role(Role.USER)
                .email("배@email.com")
                .name("배주영")
                .build());
        User friend2 = userRepository.save(User.builder()
                .sid("345")
                .name("오인혁")
                .email("test3@example.com")
                .build());
        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        Claims claims = Jwts.claims();
        claims.put("rol", Role.ADMIN.name());

        user.addUser(List.of(friend, friend2));
        friend.addUser(List.of(user));
        user.selectCourse(List.of(course));

        // when
        MvcResult mvcResult = mvc
                .perform(delete("/api/admin/form")
                        .queryParam("sid", user.getSid())
                        .requestAttr("claims", claims))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        UserDto.UserInfo res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserDto.UserInfo.class);

        // then
        assertEquals(0, res.getFriends().size());
        assertEquals(0, res.getCourses().size());
    }

    @DisplayName("그룹 미배정 학생 목록에서 미신청자도 포함한다")
    @Test
    void AdminControllerTests_122() throws Exception {
        // Given
        User userA = User.builder()
                .sub("123")
                .sid("21800012")
                .name("test")
                .role(Role.USER)
                .email("test@example.com")
                .build();

        User applicant = User.builder()
                .sub("234")
                .sid("21800345")
                .name("test2")
                .role(Role.USER)
                .email("test2@example.com")
                .build();

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        applicant.selectCourse(List.of(course));

        userRepository.save(applicant);
        userRepository.save(userA);

        Claims claims = Jwts.claims();
        claims.put("rol", Role.ADMIN.name());

        // When
        MvcResult mvcResult = mvc
                .perform(get("/api/admin/unmatched-users")
                        .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andReturn();

        List<UserDto.UserInfo> res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        // Then
        assertEquals(2, res.size());
    }

    @DisplayName("그룹을 새로 생성해 배정할 수 있다")
    @Test
    void AdminControllerTests_170() throws Exception {
        // given
        User userA = User.builder()
                .sid("201511111")
                .name("userA")
                .email("userA@test.com")
                .role(Role.USER)
                .build();
        User userB = User.builder()
                .sid("201611111")
                .name("userB")
                .email("userB@test.com")
                .role(Role.USER)
                .build();
        Course courseA = Course.builder()
                .name("courseA")
                .build();
        Course courseB = Course.builder()
                .name("courseB")
                .build();

        User save1 = userRepository.save(userA);
        User save2 = userRepository.save(userB);
        Course savedCourse1 = courseRepository.save(courseA);
        Course savedCourse2 = courseRepository.save(courseB);

        Claims claimsB = Jwts.claims();
        claimsB.put("sub", userB.getEmail());

        save1.selectCourse(List.of(savedCourse1, savedCourse2));
        save2.selectCourse(List.of(savedCourse1, savedCourse2));
        new StudyGroup(1, List.of(save1, save2));

        Claims claimAdmin = Jwts.claims();
        claimAdmin.put("rol", Role.ADMIN.name());

        UserDto.UserEdit editForm = UserDto.UserEdit.builder()
                .userId(save2.getUserId())
                .team(2)
                .build();

        // when
        MvcResult mvcResult = mvc
                .perform(post("/api/admin/edit-user")
                        .requestAttr("claims", claimAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(editForm)))
                .andExpect(status().isOk())
                .andReturn();

        UserDto.UserInfo res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserDto.UserInfo.class);

        // then
        assertEquals(2, res.getGroup());
        assertEquals(userB.getSid(), res.getSid());
        assertEquals(1, save1.getStudyGroup().getMembers().size());
    }
}
