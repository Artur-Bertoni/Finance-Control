package com.financeControl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import java.util.TimeZone;

@SpringBootApplication (exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class FinanceControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceControlApplication.class, args);
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }
}
