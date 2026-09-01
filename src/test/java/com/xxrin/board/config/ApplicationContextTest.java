package com.xxrin.board.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

@SpringJUnitConfig
@ContextConfiguration(classes = RootConfig.class)
@TestPropertySource(properties = {
        "db.driver=org.h2.Driver",
        "db.url=jdbc:h2:mem:board;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "db.username=sa",
        "db.password=",
        "hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "hibernate.hbm2ddl.auto=create-drop",
        "hibernate.show_sql=false",
        "hibernate.format_sql=false"
})
class ApplicationContextTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void rootContextProvidesJpaInfrastructure() {
        assertThat(dataSource).isNotNull();
        assertThat(entityManagerFactory.isOpen()).isTrue();
        assertThat(transactionManager).isNotNull();
    }

    @Test
    void initializerSeparatesRootAndServletContexts() {
        WebAppInitializer initializer = new WebAppInitializer();

        assertThat(initializer.getRootConfigClasses()).containsExactly(RootConfig.class);
        assertThat(initializer.getServletConfigClasses()).containsExactly(WebConfig.class);
        assertThat(initializer.getServletMappings()).containsExactly("/");
    }
}
