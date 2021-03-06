Platypus Knowledge Base Lucene Service
======================================

[![example branch parameter](https://github.com/askplatypus/platypus-kb-lucene/actions/workflows/build.yml/badge.svg)](https://github.com/askplatypus/platypus-kb-lucene/actions/workflows/build.yml)

This repository contains a Lucene-based service for the knowledge base used by Platypus.

This knowledge base is currently using Wikidata content.

To see the provided API methods go to [http://kb.askplatyp.us/api](http://kb.askplatyp.us/api)

## Install

This service depends on Java 8 and some maven dependencies. To run it you should have Java 8 and maven installed on your machine then in the root directory of the project run the commands:
```
mvn compile
mvn exec:java
```

The service will run and fill its database with the latest Wikidata dumps.

To see the API documentation (based on Swagger) browse the base URI of your installation.

If you want to create a docker image of the service just run:
```
mvn clean package docker:build
```

It creates an image called `platypus-kb-lucence` with the service listening to port `4567`.

### Configuration

The main configuration file is `src/main/resources/config.properties`.

The following configuration variables are available:

* `us.askplatyp.kb.lucene.http.uri`: the server base URI. By default `http://localhost:4567`.
* `us.askplatyp.kb.lucene.lucene.directory`: the directory where Lucene should store its files. By default the relative directory `data`.
* `us.askplatyp.kb.lucene.wikidata.directory` directory where to store a `dumpfiles` directory containing downloaded Wikidata dumps. Default value : `user.dir` configuration variable.
