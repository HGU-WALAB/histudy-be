package edu.handong.csee.histudy.domain;

import io.jsonwebtoken.Claims;
import java.util.Arrays;

public enum Role {
  ADMIN,
  MEMBER,
  USER;

  private static final String ROLE = "rol";

  public static boolean isAuthorized(Claims claims, Role... role) {
    String rol = claims.get(ROLE, String.class);
    return Arrays.stream(role).anyMatch(r -> r.name().equals(rol));
  }

  public static boolean isNotAdmin(User user) {
    return !user.getRole().equals(Role.ADMIN);
  }
}
