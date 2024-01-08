allprojects {
    repositories {
        maven {
            url = uri("https://repo.nju.edu.cn/repository/maven/")
        }
    }

    apply(plugin = "java")

    configure<JavaPluginExtension> {
        sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
    }
}
