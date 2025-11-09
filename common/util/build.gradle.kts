dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.8")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.3")

    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")

    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.8")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-assertions-json:5.8.0")
}
