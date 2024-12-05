package com.nicha.etl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
        basePackages = "com.nicha.etl.repository.defaults",
        entityManagerFactoryRef = "defaultDatabaseEntityManager",
        transactionManagerRef = "defaultDatabaseTransactionManager"
)
public class DefaultDataSourceAutoConfiguration {

    @Autowired
    private Environment env;

    @Bean
    @Primary
    public DataSource defaultDataSource() {
        DriverManagerDataSource dataSource
                = new DriverManagerDataSource();
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        return dataSource;
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean defaultDatabaseEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(defaultDataSource());
        em.setPackagesToScan(
                new String[] { "com.nicha.etl.entity.defaults" });

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
//        properties.put("logging.level.org.hibernate.SQL", "DEBUG");
//        properties.put("logging.level.org.hibernate.type.descriptor.sql.BasicBinder", "TRACE");
        em.setJpaPropertyMap(properties);

        return em;
    }


    @Bean
    @Primary
    public PlatformTransactionManager defaultDatabaseTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
                defaultDatabaseEntityManager().getObject());
        return transactionManager;
    }
}
