plugins {
    id("fabric-loom") version "0.5.43"
    id("org.cadixdev.licenser") version "0.5.0"
}

base.archivesBaseName = "mantle"
group = "slimeknights"
version = "1.6-SNAPSHOT"

repositories {
    maven {
        name = "Curseforge Maven"
        url = uri("https://www.cursemaven.com")
    }
}

val modImplementationAndInclude by configurations.register("modImplementationAndInclude")

dependencies {
    minecraft("net.minecraft", "minecraft", "1.16.5")
    mappings("net.fabricmc", "yarn", "1.16.5+build.6", classifier = "v2")

    modImplementation("net.fabricmc", "fabric-loader", "0.11.3")
    modImplementation("net.fabricmc.fabric-api", "fabric-api", "0.32.5+1.16")

    modRuntime("me.shedaniel", "RoughlyEnoughItems", "5.8.10")
    modRuntime("curse.maven", "worldedit-225608", "3135186")
    modRuntime("curse.maven", "appleskin-248787", "2987255")
    modRuntime("curse.maven", "hwyla-253449", "3033613")

    add(sourceSets.main.get().getTaskName("mod", JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME), modImplementationAndInclude)
    add(net.fabricmc.loom.util.Constants.Configurations.INCLUDE, modImplementationAndInclude)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

loom {
//    accessWidener = file("src/main/resources/mantle.aw")
}

license {
    header = file("HEADER")
    include("**/*.java")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    if (JavaVersion.current().isJava9Compatible) {
        options.release.set(8)
    } else {
        sourceCompatibility = "8"
        targetCompatibility = "8"
    }
}

tasks.withType<AbstractArchiveTask> {
    from(file("LICENSE"))
    from(file("LICENSE.LESSER"))
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.remapJar {
    doLast {
        input.get().asFile.delete()
    }
}