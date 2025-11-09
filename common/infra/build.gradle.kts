dependencies {
    implementation(project(":common:util"))

    implementation("org.springframework.boot:spring-boot-starter-web:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-cache:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-security:3.3.8")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.3")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.5")
    implementation("io.github.openfeign:feign-okhttp:13.1")

    implementation("io.micrometer:micrometer-core:1.13.9")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.8")
}
