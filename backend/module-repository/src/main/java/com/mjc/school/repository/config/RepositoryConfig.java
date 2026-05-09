package com.mjc.school.repository.config;


import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EnableJpaRepositories("com.mjc.school.repository")
@EntityScan(basePackages = {"com.mjc.school.repository.model","com.mjc.school.repository.airepo.model"})
public class RepositoryConfig {
}
