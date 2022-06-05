package com.mycompany.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.log4j.Log4j2;

//Redis는 Key, Value 구조의 비정형 데이터를 저장하고 관리하기 위한 오픈소스 기반의 비관계형 DBMS
@Log4j2
@Configuration
public class RedisConfig {
  @Value("${spring.redis.hostName}")
  private String hostName;
  
  @Value("${spring.redis.port}")
  private String port;
  
  @Value("${spring.redis.password}")
  private String password;
  
  //Redis서버와의 통신 추상화를 제공
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    log.info("실행");
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    config.setHostName(hostName);
    config.setPort(Integer.parseInt(port));
    config.setPassword(password);
    LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
    return connectionFactory;
  }
  
  //Redis서버에 데이터 CRUD를 위한 인터페이스를 제공
  @Bean
  public RedisTemplate<String, String> redisTemplate(){
    log.info("실행");
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory());
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    return redisTemplate;
  }
}
