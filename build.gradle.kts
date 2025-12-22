/* ------------------------------ Plugins ------------------------------ */
plugins {
    id("java") // Import Java plugin.
    id("java-library") // Import Java Library plugin.
    id("com.diffplug.spotless") version "8.1.0" // Import Spotless plugin.
    id("com.gradleup.shadow") version "8.3.9" // Import Shadow plugin.
    id("checkstyle") // Import Checkstyle plugin.
    eclipse // Import Eclipse plugin.
    kotlin("jvm") version "2.1.21" // Import Kotlin JVM plugin.
}

extra["kotlinAttribute"] = Attribute.of("kotlin-tag", Boolean::class.javaObjectType)

val kotlinAttribute: Attribute<Boolean> by rootProject.extra

/* --------------------------- JDK / Kotlin ---------------------------- */
java {
    sourceCompatibility = JavaVersion.VERSION_17 // Compile with JDK 17 compatibility.
    toolchain { // Select Java toolchain.
        languageVersion.set(JavaLanguageVersion.of(17)) // Use JDK 17.
        vendor.set(JvmVendorSpec.GRAAL_VM) // Use GraalVM CE.
    }
}

/* ----------------------------- Metadata ------------------------------ */
group = "io.myzticbean.FindItemAddOnOG" // Declare bundle identifier.

version = "1.0" // Declare plugin version (will be in .jar).

val apiVersion = "1.19" // Declare minecraft server target version.
val platformApi = "org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT"

/* ----------------------------- Resources ----------------------------- */
tasks.named<ProcessResources>("processResources") {
    val inputsProps =
        mapOf(
            "version" to version.toString(),
            "apiVersion" to apiVersion,
            "projectName" to project.name,
            "projectGroup" to project.group.toString(),
        )
    val templateProps =
        mapOf(
            "version" to version.toString(),
            "apiVersion" to apiVersion,
            "project" to mapOf("name" to project.name, "group" to project.group.toString()),
        )
    inputs.properties(inputsProps) // Indicates to rerun if version changes.
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand(templateProps) }
    from("LICENSE") { into("/") } // Bundle licenses into jarfiles.
}

val copyReadme by
    tasks.registering(Copy::class) {
        val templateProps =
            mapOf(
                "version" to version.toString(),
                "apiVersion" to apiVersion,
                "project" to mapOf("name" to project.name, "group" to project.group.toString()),
            )
        from("src/main/doc") {
            include("README.md")
            expand(templateProps)
        }
        into(layout.buildDirectory.dir("generated/readme"))
    }

/* ---------------------- Java project deps ---------------------------- */
dependencies {
    implementation("com.ghostchu.quickshop.compatibility:common:5.2.0.7") {
        exclude(group = "org.jetbrains", module = "annotations")
        exclude(group = "net.kyori", module = "adventure-platform-bukkit")
        exclude(group = "com.github.juliomarcopineda", module = "jdbc-stream")
        exclude(group = "one.util", module = "streamex")
        exclude(group = "tne.tne", module = "TheNewEconomy")
        exclude(group = "me.xanium", module = "GemsEconomy")
    }
    compileOnly("com.ghostchu:quickshop-bukkit:5.2.0.5:shaded") {
        exclude(group = "org.jetbrains", module = "annotations")
        exclude(group = "net.kyori", module = "adventure-platform-bukkit")
        exclude(group = "com.github.juliomarcopineda", module = "jdbc-stream")
        exclude(group = "one.util", module = "streamex")
        exclude(group = "tne.tne", module = "TheNewEconomy")
        exclude(group = "me.xanium", module = "GemsEconomy")
    }
    compileOnly(platformApi) // Declare Purpur API version to be packaged.
    compileOnly("com.ghostchu:quickshop-api:5.2.0.7") { // Import QuickShop-Hikari Core API.
        exclude(group = "org.jetbrains", module = "annotations")
        exclude(group = "net.kyori", module = "adventure-platform-bukkit")
        exclude(group = "com.github.juliomarcopineda", module = "jdbc-stream")
        exclude(group = "one.util", module = "streamex")
        exclude(group = "tne.tne", module = "TheNewEconomy")
        exclude(group = "me.xanium", module = "GemsEconomy")
    }
    compileOnly("com.github.true-og.OpenInv:openinvapi:a85e8ebc28")
    compileOnly("org.maxgamer:QuickShop:5.1.2.5-SNAPSHOT") { isTransitive = false }
    implementation("io.papermc:paperlib:1.0.7")
    implementation("com.github.KodySimpson:SimpAPI:4.6.1")
    compileOnly("net.essentialsx:EssentialsX:2.19.4") { isTransitive = false }
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.8") { // Import WorldGuard API.
        exclude(group = "com.google.code.findbugs", module = "annotations")
        exclude(group = "net.java.truecommons", module = "truecommons-logging")
        exclude(group = "org.slf4j", module = "jcl-over-slf4j")
        exclude(group = "org.bstats", module = "bstats-bukkit")
        exclude(group = "org.bukkit", module = "bukkit")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.yaml", module = "snakeyaml")
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        exclude(group = "org.spigotmc", module = "spigot-api")
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "net.java.truevfs", module = "truevfs-comp-zip")
        exclude(group = "net.java.truevfs", module = "truevfs-comp-tardriver")
    }
    implementation("com.google.code.gson:gson:2.9.0")
    compileOnly("org.jetbrains:annotations:23.0.0") { isTransitive = false }
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    implementation("org.apache.commons:commons-lang3:3.12.0") { isTransitive = false }
    compileOnly("net.kyori:adventure-api:4.16.0")
    compileOnly("cc.carm.lib:easysql-api:0.4.7")
}

apply(from = "eclipse.gradle.kts") // Import eclipse classpath support script.

tasks.matching { it.name.startsWith("eclipse") }.configureEach { dependsOn(copyReadme) }

/* ---------------------- Reproducible jars ---------------------------- */
tasks.withType<AbstractArchiveTask>().configureEach { // Ensure reproducible .jars
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

/* ----------------------------- Shadow -------------------------------- */
tasks.shadowJar {
    exclude("io.github.miniplaceholders.*") // Exclude the MiniPlaceholders package from being shadowed.
    archiveClassifier.set("") // Use empty string instead of null.
    minimize()
}

tasks.jar { archiveClassifier.set("part") } // Applies to root jarfile only.

tasks.build { dependsOn(tasks.shadowJar, copyReadme) } // Build depends on spotless and shadow.

/* --------------------------- Javac opts ------------------------------- */
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters") // Enable reflection for java code.
    options.isFork = true // Run javac in its own process.
    options.compilerArgs.add("-Xlint:deprecation") // Trigger deprecation warning messages.
    options.encoding = "UTF-8" // Use UTF-8 file encoding.
}

/* ----------------------------- Auto Formatting ------------------------ */
spotless {
    java {
        eclipse().configFile("config/formatter/eclipse-java-formatter.xml") // Eclipse java formatting.
        leadingTabsToSpaces() // Convert leftover leading tabs to spaces.
        removeUnusedImports() // Remove imports that aren't being called.
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) } // JetBrains Kotlin formatting.
        target("build.gradle.kts", "settings.gradle.kts") // Gradle files to format.
    }
}

checkstyle {
    toolVersion = "10.18.1" // Declare checkstyle version to use.
    configFile = file("config/checkstyle/checkstyle.xml") // Point checkstyle to config file.
    isIgnoreFailures = true // Don't fail the build if checkstyle does not pass.
    isShowViolations = true // Show the violations in any IDE with the checkstyle plugin.
}

tasks.named("compileJava") {
    dependsOn("spotlessApply") // Run spotless before compiling with the JDK.
}

tasks.named("spotlessCheck") {
    dependsOn("spotlessApply") // Run spotless before checking if spotless ran.
}

/* ------------------------------ Eclipse SHIM ------------------------- */

// This can't be put in eclipse.gradle.kts because Gradle is weird.
subprojects {
    apply(plugin = "java-library")
    apply(plugin = "eclipse")
    eclipse.project.name = "${project.name}-${rootProject.name}"
    tasks.withType<Jar>().configureEach { archiveBaseName.set("${project.name}-${rootProject.name}") }
}
