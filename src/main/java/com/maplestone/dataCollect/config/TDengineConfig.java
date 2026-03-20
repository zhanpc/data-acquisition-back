package com.maplestone.dataCollect.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

/**
 * 双数据源配置：
 * 1. MySQL 作为主数据源，供 MyBatis / MyBatis-Plus 使用
 * 2. TDengine 作为独立数据源，供 JdbcTemplate 使用
 */
@Configuration
@EnableConfigurationProperties(TDengineConnectionProperties.class)
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

    @Bean(name = "tdengineDataSource")
    public DataSource tdengineDataSource(TDengineConnectionProperties properties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(resolveDriverClassName(properties));
        dataSource.setJdbcUrl(buildJdbcUrl(properties));

        if (StringUtils.hasText(properties.getUsername())) {
            dataSource.setUsername(properties.getUsername());
        }
        if (StringUtils.hasText(properties.getPassword())) {
            dataSource.setPassword(properties.getPassword());
        }

        return dataSource;
    }

    @Bean(name = "tdengineJdbcTemplate")
    public JdbcTemplate tdengineJdbcTemplate(@Qualifier("tdengineDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private String resolveDriverClassName(TDengineConnectionProperties properties) {
        if (isWsMode(properties.getMode())) {
            return properties.getWs().getDriverClassName();
        }
        return properties.getNativeConfig().getDriverClassName();
    }

    private String buildJdbcUrl(TDengineConnectionProperties properties) {
        String database = defaultIfBlank(properties.getDatabase(), "iot_data");
        String charset = defaultIfBlank(properties.getCharset(), "UTF-8");
        String locale = defaultIfBlank(properties.getLocale(), "en_US.UTF-8");
        String timezone = defaultIfBlank(properties.getTimezone(), "UTC-8");

        if (isWsMode(properties.getMode())) {
            return String.format("jdbc:TAOS-RS://%s:%d/%s?charset=%s&locale=%s&timezone=%s",
                    properties.getWs().getHost(),
                    properties.getWs().getPort(),
                    database,
                    charset,
                    locale,
                    timezone);
        }

        return String.format("jdbc:TAOS://%s:%d/%s?charset=%s&locale=%s&timezone=%s",
                properties.getNativeConfig().getHost(),
                properties.getNativeConfig().getPort(),
                database,
                charset,
                locale,
                timezone);
    }

    private boolean isWsMode(String mode) {
        return "ws".equalsIgnoreCase(mode);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
