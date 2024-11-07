package com.auction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

import static com.auction.common.constants.BatchConst.MASTER_DATASOURCE;
import static com.auction.common.constants.BatchConst.SLAVE_DATASOURCE;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(MASTER_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource MASTER_DATASOURCE() {
        return DataSourceBuilder.create()
                .build();
    }

    @Bean(SLAVE_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create()
                .build();
    }
}
