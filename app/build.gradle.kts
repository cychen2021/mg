plugins {
    id("application")
}

application {
    mainClass.set("xyz.cychen.ycc.app.App")
}

dependencies {
    implementation(project(":framework"));
    implementation(project(":impl"));

    implementation("org.javatuples:javatuples:1.2")
    implementation("org.dom4j:dom4j:2.1.3")
    implementation("commons-cli:commons-cli:1.5.0")
    implementation("me.tongfei:progressbar:0.9.3")
}
