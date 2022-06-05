package com.mycompany.backend.config;


import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class DataSourceConfig {
  //Bean 어노테이션은 밑에 메소드를 실행하고 생성되는 객체를 관리객체로 만들어줌
  @Bean
  public DataSource dataSource() {
    log.info("실행");
    HikariConfig config = new HikariConfig();
    config.setDriverClassName("oracle.jdbc.OracleDriver");
    config.setJdbcUrl("jdbc:oracle:thin:@kosa1.iptime.org:50112:orcl");
    config.setUsername("spring");
    config.setPassword("oracle");
    config.setMaximumPoolSize(3);
    HikariDataSource hikariDataSource = new HikariDataSource(config);
    return hikariDataSource;
  }
}