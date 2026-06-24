plugins {
    java
    id("org.openjfx.javafxplugin")
}
javafx { version = "23.0.2"; modules = listOf("javafx.controls") }

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:settings"))
    implementation(project(":modules:tools-api"))
    implementation("org.apache.commons:commons-text:1.15.0")
}
