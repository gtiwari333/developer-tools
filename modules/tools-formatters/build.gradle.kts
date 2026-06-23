plugins { java }

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:settings"))
    implementation(project(":modules:tools-api"))
    implementation("com.github.vertical-blank:sql-formatter:2.0.5")
}
