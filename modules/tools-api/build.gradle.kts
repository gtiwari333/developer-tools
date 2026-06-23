plugins { java }

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:settings"))
    implementation("com.formdev:flatlaf:3.6")
    implementation("com.fifesoft:rsyntaxtextarea:3.6.0")
    implementation("org.slf4j:slf4j-simple:2.0.18")
}
