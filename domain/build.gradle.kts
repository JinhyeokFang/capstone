dependencies {
    implementation(project(":common:util"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.8")
}
