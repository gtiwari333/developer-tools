plugins {
    java
    id("org.openjfx.javafxplugin")
}
javafx { version = "23.0.2"; modules = listOf("javafx.controls") }

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:settings"))
    implementation(project(":modules:tools-api"))
    implementation("commons-codec:commons-codec:1.22.0")
    implementation("org.bitbucket.b_c:jose4j:0.9.6")
}
