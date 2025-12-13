package uk.jinhy.capstone.infra.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import com.querydsl.sql.MySQLTemplates
import com.querydsl.sql.SQLQueryFactory
import com.querydsl.sql.spring.SpringConnectionProvider
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class QuerydslConfig {
    @Bean
    fun jpaQueryFactory(entityManager: EntityManager): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }

    @Bean
    fun sqlQueryFactory(
        dataSource: DataSource,
    ): SQLQueryFactory {
        val sqlTemplates = MySQLTemplates.builder().build()
        val queryDslConfiguration = com.querydsl.sql.Configuration(sqlTemplates)
        return SQLQueryFactory(queryDslConfiguration, SpringConnectionProvider(dataSource))
    }
}
