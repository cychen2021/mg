plugins {
    id("java-library")
}

dependencies {
    implementation(project(":framework"))
    implementation("org.javatuples:javatuples:1.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation(project(":app"))
}

tasks.test {
    useJUnitPlatform()
}
