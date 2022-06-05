package com.mycompany.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.backend.dto.Member;
import com.mycompany.backend.security.Jwt;
import com.mycompany.backend.service.MemberService;
import com.mycompany.backend.service.MemberService.JoinResult;
import com.mycompany.backend.service.MemberService.LoginResult;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/member")
public class MemberController {
  @Resource
  private MemberService memberService;
  
  @Resource
  private PasswordEncoder passwordEncoder;
  
  @Resource
  private RedisTemplate<String, String> redisTemplate;
  
  //회원가입
  @PostMapping("/join")
  public Map<String, Object> join(@RequestBody Member member){ //RequestBody를 사용하면 요청시 queryString을 사용하면 안됨
    //계정 활설화
    member.setMenabled(true);
    //패스워드 암호화
    member.setMpassword(passwordEncoder.encode(member.getMpassword()));
    //회원 가입 처리
    JoinResult joinResult = memberService.join(member);
    //응답 내용 설정
    Map<String, Object> map = new HashMap<>();
    if(joinResult == JoinResult.SUCCESS) {
      map.put("result", "success");
    } else if(joinResult == JoinResult.DUPLICATED){
      map.put("result", "duplicated");
    } else {
      map.put("result", "fail");
    }
    return map;
  }
  
  //로그인
  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody Member member){
    log.info("실행");
    
    //mid와 mpassword가 없을 경우
    if(member.getMid() == null || member.getMpassword() == null) {
      //인증에러 401
      return ResponseEntity
                .status(401)
                .body("mid or mpassword cannot be null");
    }
    
    //로그인 결과 얻기
    LoginResult loginResult = memberService.login(member);
    
    if(loginResult != LoginResult.SUCCESS) {
      return ResponseEntity
                .status(401)
                .body("mid or mpassword is wrong");
    }
    
    Member dbMember = memberService.getMember(member.getMid());
    
    String accessToken = Jwt.createAccessToken(member.getMid(), dbMember.getMrole());
    String refreshToken = Jwt.createRefreshToken(member.getMid(), dbMember.getMrole());
    
    //Redis에 저장
    ValueOperations<String, String> vo = redisTemplate.opsForValue();
    vo.set(accessToken, refreshToken, Jwt.REFRESH_TOKEN_DURATION, TimeUnit.MILLISECONDS);//키에 accessToken, 값에 refreshToken
    
    //쿠키생성
    String refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                                              .httpOnly(true)
                                              .secure(false)
                                              .path("/")
                                              .maxAge(Jwt.REFRESH_TOKEN_DURATION/1000)
                                              .domain("localhost")
                                              .build()
                                              .toString();
    //본문 생성
    String json = new JSONObject()
                        .put("accessToken", accessToken)
                        .put("mid", member.getMid())
                        .toString();
    //응답 설정
    return ResponseEntity 
                  //응답 상태 코드 : 200
                  .ok()
                  //응답 헤더 추가
                  .header(HttpHeaders.SET_COOKIE,refreshTokenCookie)
                  .header(HttpHeaders.CONTENT_TYPE, "application/json")
                  //응답 바디 추가
                  .body(json);
  }
  
  //로그아웃
  @GetMapping("/logout")
  public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorization){
    //AccessToken 얻기
    String accessToken = Jwt.getAccessToken(authorization);
    if(accessToken == null) {
      return ResponseEntity.status(401).body("invalide access token");
    }
    
    //Redis에 저장된 인증 정보를 삭제
    redisTemplate.delete(accessToken);
    
    //RefreshToken 쿠키 삭제
    String refreshTokenCookie = ResponseCookie.from("refreshToken", "")
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(0)
        .domain("localhost")
        .build()
        .toString();
    
    //응답 설정
    return ResponseEntity
              .ok()
              .header(HttpHeaders.SET_COOKIE, refreshTokenCookie)
              .body("success");
  }
  
}
