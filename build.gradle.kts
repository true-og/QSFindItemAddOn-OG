plugins {
    id("java") // Tell gradle this is a java project.
    id("java-library") // Import helper for source-based libraries.
    id("com.diffplug.spotless") version "7.0.4"
    id("com.gradleup.shadow") version "8.3.6" // Import shadow API.
    eclipse // Import eclipse plugin for IDE integration.
}

group = "io.myzticbean.finditemaddon"

version = "2.0.8"

val apiVersion = "1.19"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version, "apiVersion" to apiVersion)

    inputs.properties(props) // Indicates to rerun if version changes.

    filesMatching("plugin.yml") { expand(props) }
    from("LICENSE") { into("/") }
    from("src/main/doc/README.md") { into("/") }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://repo.purpurmc.org/snapshots") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://jitpack.io") }
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.essentialsx.net/snapshots/")
    maven("https://repo.olziedev.com/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.io/repository/maven-public/") {
        metadataSources {
            mavenPom()
            artifact()
        }
    }
    maven("https://repo.codemc.io/repository/bentoboxworld/") {
        metadataSources {
            mavenPom()
            artifact()
        }
    }
}

val playerwarpsApiOld = "6.30.0"
val playerwarpsApiNew = "7.7.1"

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT") // Declare purpur API version to be packaged.
    compileOnly("com.ghostchu:quickshop-api:6.2.0.5")
    compileOnly("com.ghostchu:quickshop-bukkit:6.2.0.5:shaded") {
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
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7") {
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
    compileOnly(files("lib/Residence5.1.5.1.jar"))
    compileOnly(files("lib/GPFlags-5.13.4.jar"))
    compileOnlyApi(project(":libs:Utilities-OG"))
    implementation("io.papermc:paperlib:1.0.7")
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.jetbrains:annotations:24.1.0")
}

tasks.withType<AbstractArchiveTask>().configureEach { // Ensure reproducible .jars
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    exclude("io.github.miniplaceholders.*") // Exclude the MiniPlaceholders package from being shadowed.
    archiveClassifier.set("") // Use empty string instead of null.
    minimize()
}

tasks.build {
    dependsOn(tasks.spotlessApply)
    dependsOn(tasks.shadowJar)
}

tasks.jar { archiveClassifier.set("part") }

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Xlint:deprecation") // Triggers deprecation warning messages.
    options.encoding = "UTF-8"
    options.isFork = true
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}

spotless {
    java {
        removeUnusedImports()
        palantirJavaFormat()
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
        target("build.gradle.kts", "settings.gradle.kts")
    }
}
