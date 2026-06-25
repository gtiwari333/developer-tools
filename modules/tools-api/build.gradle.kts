plugins {
    java
    id("org.openjfx.javafxplugin")
}

javafx {
    version = "23.0.2"
    modules = listOf("javafx.controls", "javafx.graphics")
}

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:settings"))
    implementation("org.slf4j:slf4j-simple:2.0.18")
}
