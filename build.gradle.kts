import dev.kordex.gradle.plugins.kordex.DataCollection

plugins {
	idea
	distribution

	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)

	alias(libs.plugins.detekt)

	alias(libs.plugins.kordex.plugin)
}

val javaVersion = 21

group = "dev.upcraft.tourguide"
version = System.getenv("VERSION") ?: "1.0.0-SNAPSHOT"

dependencies {
	detektPlugins(libs.detekt)

	implementation(libs.kotlin.stdlib)
	implementation(libs.kx.ser)

	// Logging dependencies
	implementation(libs.groovy)
	implementation(libs.jansi)
	implementation(libs.logback)
	implementation(libs.logback.groovy)
	implementation(libs.logging)
}

// Configure distributions plugin
distributions {
	main {
		distributionBaseName = project.name

		contents {
			// Copy the LICENSE file(s) into the distribution
			from(rootProject.file("LICENSE.md"))

			// Exclude src/main/dist/README.md
			exclude("README.md")
		}
	}
}

kordEx {
	// https://github.com/gradle/gradle/issues/31383
	kordExVersion = libs.versions.kordex.asProvider()

	bot {
		// See https://docs.kordex.dev/data-collection.html
		dataCollection(DataCollection.Minimal)

		mainClass = "dev.upcraft.tourguide.AppKt"
	}

	i18n {
		classPackage = "dev.upcraft.tourguide.i18n"
		translationBundle = "tourguide.strings"
	}
}

detekt {
	buildUponDefaultConfig = true

	config.from(rootProject.files("detekt.yml"))
}
kotlin {
	jvmToolchain {
		languageVersion.set(JavaLanguageVersion.of(javaVersion))
	}
}

tasks.withType<Jar> {
	manifest {
		attributes["Implementation-Title"] = project.name
		attributes["Specification-Version"] = project.version

		System.getenv("COMMIT_SHA_SHORT")?.let { sha ->
			attributes["Implementation-Version"] = sha
		}
	}
}
tasks.assemble {
	dependsOn(tasks["installDist"])
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}
