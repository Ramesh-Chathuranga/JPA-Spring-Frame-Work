package lk.ijse.dep.app.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@PropertySource("file:${user.dir}/Application Architecture +Spring+JPA/settings/application.properties")
public class JPAConfig {
    @Autowired
    private Environment env;

    @Bean
   public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, JpaVendorAdapter adapter){
       LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
       localContainerEntityManagerFactoryBean.setJpaVendorAdapter(adapter);
       localContainerEntityManagerFactoryBean.setDataSource(dataSource);
       localContainerEntityManagerFactoryBean.setPackagesToScan("lk.ijse.dep.app.entity");
       return localContainerEntityManagerFactoryBean;
   }

   @Bean
   public DataSource dataSource(){
       DriverManagerDataSource source = new DriverManagerDataSource();
       source.setDriverClassName("com.mysql.jdbc.Driver");
       source.setUsername(env.getRequiredProperty("javax.persistence.jdbc.user"));
       source.setPassword(env.getRequiredProperty("javax.persistence.jdbc.password"));
       source.setUrl(env.getRequiredProperty("javax.persistence.jdbc.url"));
       return source;
   }
   @Bean
   public JpaVendorAdapter jpaVendorAdapter(){
       HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
       jpaVendorAdapter.setGenerateDdl(true);
       jpaVendorAdapter.setShowSql(true);
       jpaVendorAdapter.setDatabase(Database.MYSQL);
       jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL57Dialect");
       return jpaVendorAdapter;
   }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf){
        return new JpaTransactionManager(emf);
    }

}
