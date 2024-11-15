# Slate *(for Kotlin)*

![Environment](https://img.shields.io/badge/Enviroment-Server-black)
[![Discord](https://img.shields.io/discord/1035236362263207936?color=blue&logo=discord&label=Discord)](https://discord.mcbrawls.net)

**Slate** is a library for server-side developers which makes creating clean server-side screens easy.

# Usage

Public builds of Slate can be found on the [Modrinth](https://modrinth.com/mod/slates) page.

To use Slate as a dependency you can find the artefact on our [public Maven repository](https://maven.andante.dev/#/releases/net/mcbrawls/slate).

`build.gradle`
```gradle
repositories {
    maven {
        name = "Andante's Maven"
        url  = "https://maven.andante.dev/releases/"
    }
}

dependencies {
    include modImplementation ("net.mcbrawls:slate:$slate_version")
}
```

`gradle.properties`
```properties
slate_version=1.0
```

# Getting Started

Slate is designed to be super simple.

```kt
val slate = slate()
slate.open(player)
```

This is all you need to create a slate! **To find out how to customise your slate, [see our wiki](https://github.com/mcbrawls/slate/wiki/Creating-Your-First-Slate).**
