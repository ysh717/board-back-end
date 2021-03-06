package com.mycompany.backend.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Jwt {
  //상수
  private static final String JWT_SECRET_KEY = "kosa12345";                     //매우 중요! 암호화 하여 사용해야된다.
  private static final long ACCESS_TOKEN_DURATION = 1000 * 60 *30;             //AccessToken의 유효시간 30분
  public static final long REFRESH_TOKEN_DURATION = 1000 * 60 * 60 * 24;        //RefreshToken의 유효시간 24시간
  
  //AccessToken 생성
  public static String createAccessToken(String mid, String authority) {
    log.info("실행");
    String accessToken = null;
    
    try {
      accessToken = Jwts.builder()
                        //헤더설정
                        .setHeaderParam("alg", "HS256")
                        .setHeaderParam("typ", "JWT")
                        //페이로드설정
                        .setExpiration(new Date(new Date().getTime() + ACCESS_TOKEN_DURATION)) //유효기간 설정
                        .claim("mid", mid)
                        .claim("authority", authority)
                        //서명 설정
                        .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY.getBytes("UTF-8"))
                        .compact();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    
    return accessToken;
  }
  //RefreshToken 생성
  public static String createRefreshToken(String mid, String authority) {
    log.info("실행");
    String refreshToken = null;
    
    try {
      refreshToken = Jwts.builder()
                        //헤더설정
                        .setHeaderParam("alg", "HS256")
                        .setHeaderParam("typ", "JWT")
                        //페이로드설정
                        .setExpiration(new Date(new Date().getTime() + REFRESH_TOKEN_DURATION)) //유효기간 설정
                        .claim("mid", mid)
                        .claim("authority", authority)
                        //서명 설정
                        .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY.getBytes("UTF-8"))
                        .compact();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    
    return refreshToken;
  }
  
  //토큰 유효성 판단
  public static boolean validateToken(String token) {
    log.info("실행");
    boolean result = false;
    
    try {
      result = Jwts.parser()
          .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8"))
          .parseClaimsJws(token)
          .getBody()
          .getExpiration()
          .after(new Date());
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    
    return result;
  }
  
  //토크 만료 날짜 얻기
  public static Date getExpiration(String token) {
    log.info("실행");
    Date result = null;
    
    try {
      result = Jwts.parser()
          .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8"))
          .parseClaimsJws(token)
          .getBody()
          .getExpiration();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    
    return result;
  }
  
  //인증 사용자 정보 얻기
  public static Map<String, String> getUserInfo(String token){
    log.info("실행");
    Map<String, String> result = new HashMap<>();
    
    try {
      Claims claims = Jwts.parser()
          .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8"))
          .parseClaimsJws(token)
          .getBody();
      result.put("mid", claims.get("mid", String.class));
      result.put("authority", claims.get("authority", String.class));
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    
    return result;
  }
  
  //요청 Authorization 헤더값에서 AccessToken 얻기
  //Bearer xxxxxxxxxxxxxxx.xxxxxxxxxxxxxxx.xxxxxxxxxxxxxxx
  public static String getAccessToken(String authorization) {
    String accessToken = null;
    
    if(authorization != null && authorization.startsWith("Bearer")) {
      accessToken = authorization.substring(7);
    }
    
    return accessToken;
  }
  
  public static void main(String[] args) {
    String accessToken = createAccessToken("user", "ROLE_USER");
    System.out.println(validateToken(accessToken));
    Date expiration = getExpiration(accessToken);
    System.out.println(expiration);
    
    Map<String, String> userInfo = getUserInfo(accessToken);
    System.out.println(userInfo);
  }
}
