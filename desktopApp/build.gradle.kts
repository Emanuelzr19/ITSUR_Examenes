import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(libs.koin.core)
                implementation(libs.voyager.navigator)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "mx.itsur.exams.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ITSURExamenes"
            packageVersion = "1.0.0"
        }
    }
}

// Fuerza renderizado SOFTWARE en la tarea run para evitar el bug RenderNodeContext en Windows
tasks.withType<JavaExec>().configureEach {
    jvmArgs("-Dskiko.renderApi=SOFTWARE")
}
