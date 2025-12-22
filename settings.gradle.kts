rootProject.name = "QSFindItemAddOn-OG"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    /* ---------------------------- Repos ---------------------------------- */
    repositories {
        mavenCentral() // Import the Maven Central Maven Repository.
        gradlePluginPortal() // Import the Gradle Plugin Portal Maven Repository.
        maven { url = uri("https://repo.purpurmc.org/snapshots") } // Import the PurpurMC Maven Repository.
        maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
        maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { url = uri("https://repo.essentialsx.net/releases/") }
        maven { url = uri("https://repo.essentialsx.net/snapshots/") }
        maven { url = uri("https://maven.enginehub.org/repo/") }
        maven { url = uri("https://repo.glaremasters.me/repository/public/") }
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://repo.olziedev.com/") }
    }
}
