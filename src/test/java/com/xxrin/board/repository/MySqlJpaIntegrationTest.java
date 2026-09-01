package com.xxrin.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class MySqlJpaIntegrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("board_db")
            .withUsername("board_user")
            .withPassword("board_password");

    private static HikariDataSource dataSource;
    private static EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void setUpJpa() {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(MYSQL.getJdbcUrl());
        hikari.setUsername(MYSQL.getUsername());
        hikari.setPassword(MYSQL.getPassword());
        hikari.setDriverClassName(MYSQL.getDriverClassName());
        dataSource = new HikariDataSource(hikari);

        LocalContainerEntityManagerFactoryBean factory =
                new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.xxrin.board.domain");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.dialect", "org.hibernate.dialect.MySQLDialect"));
        factory.afterPropertiesSet();
        entityManagerFactory = factory.getObject();
    }

    @AfterAll
    static void tearDownJpa() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    void deletingBoardCascadesToComments() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Board board = Board.builder()
                .title("제목").content("내용").writer("작성자").passwordHash("hash").build();
        Comment.builder().content("댓글").writer("댓글 작성자").passwordHash("comment-hash").board(board).build();
        entityManager.persist(board);
        entityManager.getTransaction().commit();
        Long boardId = board.getId();

        entityManager.getTransaction().begin();
        entityManager.remove(entityManager.find(Board.class, boardId));
        entityManager.getTransaction().commit();

        assertThat(entityManager.createQuery("select count(c) from Comment c", Long.class)
                .getSingleResult()).isZero();
        entityManager.close();
    }
}
