# DVCS Web UI

A [Freenet](https://freenetproject.org/) plugin providing a web interface for distributed version control.
Currently only [Infocalypse](https://bitbucket.org/operhiem1/wiki_hacking) supports it.

# Prerequisites

Requires `freenet` and `freenet-ext`. As these are not currently available in Maven repositories,
after [downloading them](https://downloads.freenetproject.org/alpha/), install them:

    mvn install:install-file -Dfile=freenet.jar -DgroupId=org.freenetproject -DartifactId=fred -Dversion=0.7.5.1451 -Dpackaging=jar
    mvn install:install-file -Dfile=freenet-ext.jar -DgroupId=org.freenetproject -DartifactId=freenet-ext -Dversion=29 -Dpackaging=jar

# Building

`mvn package`
