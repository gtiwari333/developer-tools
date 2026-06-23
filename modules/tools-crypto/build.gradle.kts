plugins { java }

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:settings"))
    implementation(project(":modules:tools-api"))
    implementation("com.fasterxml.uuid:java-uuid-generator:5.2.0")
    implementation("com.github.f4b6a3:ulid-creator:5.2.4")
}
