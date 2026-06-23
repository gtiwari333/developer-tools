plugins { java }

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:settings"))
    implementation(project(":modules:tools-api"))
    implementation("commons-codec:commons-codec:1.22.0")
}
