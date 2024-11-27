package com.nicha.etl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.nicha.etl.repository.config",
        entityManagerFactoryRef = "configDatabaseEntityManager",
        transactionManagerRef = "configDatabaseTransactionManager"
)
public class PersistenceConfigDBAutoConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public DataSource configDataSource() {
        DriverManagerDataSource dataSource
                = new DriverManagerDataSource();
        dataSource.setUrl(env.getProperty("spring.config-db.url"));
        dataSource.setUsername(env.getProperty("spring.config-db.username"));
        dataSource.setPassword(env.getProperty("spring.config-db.password"));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean configDatabaseEntityManager() {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(configDataSource());
        em.setPackagesToScan(
                new String[] { "com.nicha.etl.entity.config" });

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        em.setJpaPropertyMap(properties);

        return em;
    }


    @Bean
    public PlatformTransactionManager configDatabaseTransactionManager() {
        JpaTransactionManager transactionManager
                = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
                configDatabaseEntityManager().getObject());
        return transactionManager;
    }
}
