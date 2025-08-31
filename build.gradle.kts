/* ------------------------------ Plugins ------------------------------ */
plugins {
    id("java") // Import Java plugin.
    id("java-library") // Import Java Library plugin.
    id("com.diffplug.spotless") version "7.0.4" // Import Spotless plugin.
    id("com.gradleup.shadow") version "8.3.6" // Import Shadow plugin.
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

kotlin { jvmToolchain(17) }

/* ----------------------------- Metadata ------------------------------ */
group = "net.trueog.qsfinditemaddon-og"

version = "2.0.8"

val apiVersion = "1.19"

/* ----------------------------- Resources ----------------------------- */
tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version, "apiVersion" to apiVersion)
    inputs.properties(props) // Indicates to rerun if version changes.
    filesMatching("plugin.yml") { expand(props) }
    from("LICENSE") { into("/") } // Bundle licenses into jarfiles.
}

/* ---------------------------- Repos ---------------------------------- */
repositories {
    mavenCentral() // Import the Maven Central Maven Repository.
    gradlePluginPortal() // Import the Gradle Plugin Portal Maven Repository.
    maven { url = uri("https://repo.purpurmc.org/snapshots") } // Import the PurpurMC Maven Repository.
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    } // Import the SpigotMC Maven Repository.
    maven { url = uri("https://jitpack.io") } // Import the Jitpack Maven Repository.
    maven("https://oss.sonatype.org/content/groups/public/") // Import the OSS Sonatype Maven Repository.
    maven("https://repo.essentialsx.net/releases/") // Import the EssentialsX Maven Repository.
    maven("https://repo.olziedev.com/") // Import the Olziedev Maven Repository.
    maven("https://maven.enginehub.org/repo/") // Import the EngineHub Maven Repository.
    maven("https://repo.codemc.io/repository/maven-public/") { // Import the CodeMC Maven Repository.
        metadataSources {
            mavenPom()
            artifact()
        }
    }
    maven("https://repo.codemc.io/repository/bentoboxworld/") { // Import the BentoBox Maven Repository.
        metadataSources {
            mavenPom()
            artifact()
        }
    }
}

/* ---------------------- Java project deps ---------------------------- */
val playerwarpsApiOld = "6.30.0"
val playerwarpsApiNew = "7.7.1"

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT") // Declare Purpur API version to be packaged.
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.ghostchu:quickshop-api:5.2.0.7")
    compileOnly("com.ghostchu:quickshop-bukkit:5.2.0.7:shaded") {
        exclude("org.jetbrains", "annotations")
        exclude("net.kyori", "adventure-platform-bukkit")
        exclude("com.github.juliomarcopineda", "jdbc-stream")
        exclude("one.util", "streamex")
        exclude("tne.tne", "TheNewEconomy")
        exclude("me.xanium", "GemsEconomy")
        exclude("com.ghostchu", "quickshop-common")
    }
    compileOnly("org.maxgamer:QuickShop:5.1.2.5-SNAPSHOT") { isTransitive = false }
    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    compileOnly("com.olziedev:playerwarps-api:$playerwarpsApiOld") { exclude("*", "*") }
    compileOnly("com.olziedev:playerwarps-api:$playerwarpsApiNew")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.8") {
        exclude("org.bstats", "bstats-bukkit")
        exclude("org.slf4j", "slf4j-api")
        exclude("com.google.guava", "guava")
        exclude("org.spigotmc", "spigot-api")
        exclude("org.apache.httpcomponents", "httpclient")
    }
    compileOnly("net.kyori:adventure-api:4.16.0")
    compileOnly("cc.carm.lib:easysql-api:0.4.7")
    compileOnly("world.bentobox:bentobox:3.2.3-SNAPSHOT")
    compileOnly("com.github.GriefPrevention:GriefPrevention:16.18.4")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    compileOnly(files("libs/GPFlags-5.13.4.jar"))
    compileOnlyApi(project(":libs:Utilities-OG"))
    implementation("io.papermc:paperlib:1.0.7")
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.jetbrains:annotations:24.1.0")
}

apply(from = "eclipse.gradle.kts") // Import eclipse classpath support script.

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

tasks.build { dependsOn(tasks.spotlessApply, tasks.shadowJar) } // Build depends on spotless and shadow.

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
