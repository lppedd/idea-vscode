plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.8.21"
  id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.github.lppedd"
version = "0.0.2"

repositories {
  mavenCentral()
}

intellij {
  version.set("2023.1.2")
  type.set("IU")
  downloadSources.set(true)
  pluginName.set("idea-vscode")
  plugins.set(listOf("JavaScript", "nodeJS"))
}

tasks {
  compileJava {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }

  compileKotlin {
    kotlinOptions.jvmTarget = "17"
  }

  patchPluginXml {
    version.set(project.version.toString())
    sinceBuild.set("231")
    untilBuild.set("232.*")
  }

  runIde {
    maxHeapSize = "4g"
  }
}
