package com.maplestone.dataCollect.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 双数据源配置：
 * 1. MySQL 作为主数据源，供 MyBatis / MyBatis-Plus 使用
 * 2. TDengine 作为独立数据源，供 JdbcTemplate 使用
 */
@Configuration
public class TDengineConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataSource")
    @Primary
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "tdengine.datasource")
    public DataSourceProperties tdengineDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "tdengineDataSource")
    public DataSource tdengineDataSource() {
        return tdengineDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "tdengineJdbcTemplate")
    public JdbcTemplate tdengineJdbcTemplate(@Qualifier("tdengineDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
