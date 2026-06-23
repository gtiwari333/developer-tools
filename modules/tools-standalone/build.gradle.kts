plugins { java }

tasks.compileJava {
    // jfiglet lacks JPMS metadata — allow reading from classpath
    options.compilerArgs.addAll(listOf(
        "--add-reads", "gt.devtools.tools.standalone=ALL-UNNAMED"
    ))
}

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:settings"))
    implementation(project(":modules:tools-api"))
    implementation("tools.jackson.core:jackson-databind:3.0.2")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml:3.0.2")
    implementation("tools.jackson.dataformat:jackson-dataformat-xml:3.0.2")
    implementation("tools.jackson.dataformat:jackson-dataformat-toml:3.0.2")
    implementation("tools.jackson.dataformat:jackson-dataformat-properties:3.0.2")
    implementation("com.jayway.jsonpath:json-path:2.9.0")
    implementation("com.networknt:json-schema-validator:1.5.6")
    implementation("com.cronutils:cron-utils:9.2.1")
    implementation("com.google.zxing:core:3.5.4")
    implementation("com.google.zxing:javase:3.5.4")
    implementation("com.github.lalyos:jfiglet:0.0.9")
    implementation("org.apache.commons:commons-compress:1.28.0")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
}
