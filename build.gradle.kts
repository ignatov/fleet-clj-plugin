repositories {
    mavenCentral()
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
}

plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.fleet-plugin") version "0.2.46"
}

version = "0.1.0"

fleet {
    fleetVersion.set("1.21.20")
    useNightlyBuilds.set(true)

    // presentation
    vendor.set("Sergey Ignatov")
    readableName.set("Clojure")
    descriptor.set("A Fleet plugin for Clojure")

    workspace {
        // workspace dependencies
    }

    frontend {
        // frontend dependencies
    }

    common {
        // common dependencies
    }

    // required plugins
    plugins.addAll(
        "fleet.lsp",
        "fleet.code" // publish code
    )
}
