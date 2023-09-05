package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.UserForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.exception.ForbiddenException;
import edu.handong.csee.histudy.jwt.JwtPair;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "일반 사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<UserDto.UserLogin> createUser(@RequestBody UserForm userForm) {
        userService.signUp(userForm);
        JwtPair tokens = jwtService.issueToken(userForm.getEmail(), userForm.getName(), Role.USER);

        return ResponseEntity.ok(UserDto.UserLogin.builder()
                .isRegistered(true)
                .tokenType("Bearer ")
                .tokens(tokens)
                .role(Role.USER.name())
                .build());
    }

    /**
     * 스터디 그룹 신청 단계에서
     * 같이 스터디할 유저를 검색하는 API
     *
     * <p>원래 토큰 검증은 인터셉터에서 처리하고 있으나,
     * HTTP 메서드만 다르고 동일한 URI를 가지는
     * 회원가입 API와 요청을 구분하기가 번거로워서
     * 이 API에 한해서만 컨트롤러에 검증을 위임하였다.
     *
     * @param keyword 검색 키워드: 이름 또는 학번 또는 이메일
     * @param header  액세스 토큰
     * @return 유저 목록
     */
    @Operation(summary = "유저 검색")
    @SecurityRequirement(name = "USER")
    @GetMapping
    public ResponseEntity<UserDto> searchUser(
            @Parameter(allowEmptyValue = true) @RequestParam(name = "search") Optional<String> keyword,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) Optional<String> header) {
        String token = jwtService.extractToken(header);
        Claims claims = jwtService.validate(token);
        String email = claims.getSubject();

        List<UserDto.UserMatching> users = userService.search(keyword)
                .stream()
                .filter(Role::isNotAdmin)
                .filter(u -> !u.getEmail().equals(email))
                .map(UserDto.UserMatching::new)
                .toList();

        return ResponseEntity.ok(new UserDto(users));
    }

    @Operation(summary = "내 정보 조회")
    @SecurityRequirements({
            @SecurityRequirement(name = "USER"),
            @SecurityRequirement(name = "MEMBER"),
            @SecurityRequirement(name = "ADMIN")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto.UserMe> getMyInfo(
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.values())) {
            UserDto.UserMe info = userService.getUserMe(Optional.ofNullable(claims.getSubject()));
            return ResponseEntity.ok(info);
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "스터디 그룹 신청 정보 조회")
    @SecurityRequirement(name = "USER")
    @GetMapping("/me/forms")
    public ResponseEntity<ApplyFormDto> getMyApplicationForm(
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.USER)) {
            return ResponseEntity.ok(userService.getUserInfo(claims.getSubject()));
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "전체 유저 스터디 신청 정보 조회")
    @SecurityRequirement(name = "ADMIN")
    @Deprecated
    @GetMapping("/manageUsers")
    public List<UserDto.UserInfo> userList(
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.ADMIN)) {
            return userService.getUsers(claims.getSubject());
        }
        throw new ForbiddenException();
    }
}
