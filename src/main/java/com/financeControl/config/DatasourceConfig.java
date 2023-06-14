package com.financeControl.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

@Configuration
public class DatasourceConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        System.out.println(env.getProperty("spring.datasource.driver-class-name"));
        System.out.println(env.getProperty("spring.datasource.url"));
        System.out.println(env.getProperty("spring.datasource.username"));
        System.out.println(env.getProperty("spring.datasource.password"));

        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("spring.datasource.driver-class-name")));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));

        return dataSource;
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();

        liquibase.setChangeLog(env.getProperty("spring.liquibase.change-log"));
        liquibase.setDataSource(dataSource());
        liquibase.setLiquibaseSchema(env.getProperty("spring.liquibase.default-schema"));
        return liquibase;
    }

    @Bean(name = "entityManagerFactory")
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setHibernateProperties(new Properties() {{
            setProperty("hibernate.dialect", env.getProperty("spring.jooq.sql-dialect"));
            setProperty("hibernate.connection.url", env.getProperty("spring.datasource.url"));
            setProperty("hibernate.connection.username", env.getProperty("spring.datasource.username"));
            setProperty("hibernate.connection.password", env.getProperty("spring.datasource.password"));
            setProperty("hibernate.show_sql", "false");
        }});
        sessionFactory.setPackagesToScan("com.financeControl");

        return sessionFactory;
    }
}
