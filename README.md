Influx
=======

Welcome to **Influx** - the open source command management system, built to perform the heavy lifting behind **command syntax parsing** for you.

Influx is the continuation of **[CommandManager v2.0][cmd-manager]**. For more information on how to port your old command handlers over to Influx, see the page on **[[porting from CommandManager]]**

## Quick Links

* [Jenkins instance][ci] - Hosts automated builds of the Influx project
* [JavaDocs][jd] - API documentation

# Learning to use Influx

It is advised that you begin your journey with Influx over at the [wiki][wiki] before learning to use or contributing to the project.

## Building

```
mvn clean install
```

Influx uses [Maven 3][mvn] to manage dependencies and project building. Simply run `mvn clean install` in the appropriate directory to build this project locally. A complete `.JAR` file will be generated in the `target/` directory for you to use.

## As a dependency

Influx can be added as a dependency by adding the following to your project's `pom.xml` file:

**Influx Maven repository:**

```xml

<repositories>

    <!-- All other dependencies -->

    <!-- Influx repo -->
    <repository>
        <id>dsh-repo</id>
        <url>http://repo.dsh105.com/</url>
    </repository>

</repositories>
```

**Influx as a depdenency:**
```xml
<dependencies>

    <!-- All other dependencies -->

    <!-- Influx -->
    <dependency>
        <groupId>com.dsh105</groupId>
        <artifactId>Influx</artifactId>
        <!-- LATEST can be replaced with the desired version -->
        <version>LATEST</version>
        <!-- Shading will be covered later -->
        <scope>compile</scope>
    </dependency>

</dependencies>
```

## Need to talk to a human?

[We'd love to talk to you][talk-to-us]



[cmd-manager]: https://github.com/DSH105/CommandManager/
[ci]: http://ci.dsh105.com/job/Influx
[jd]: http://jd.dsh105.com/Influx
[wiki]: https://github.com/DSH105/Influx/
[talk-to-us]: http://github.com/DSH105/Influx/wiki/talk-to-us/
