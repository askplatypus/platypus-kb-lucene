Platypus Knowledge Base Lucene Service
======================================

[![Build Status](https://travis-ci.org/askplatypus/platypus-kb-lucene.svg?branch=master)](https://travis-ci.org/askplatypus/platypus-kb-lucene)

This repository contains a Lucene-based service for the knowledge base used by Platypus.

This knowledge base is currently using Wikidata content.


## Usage

This service provides an HTTP REST API.

All method supports content and language negotiation using `Accept` and `Accept-Language` headers.
The content types currently supported are `application/ld+json` and `application/json`.

### Methods

#### `/entity/`

Allows to retrieve an entity description based on its IRI like `/entity/wd:Q42` with a `GET` request.

#### `/search/simple`

Allows to do simple queries inside of the knowledge base. The `GET` request supports the following query parameters:
* `q` for the query itself like `Barack Obama`
* `lang` the language of the query like `en` or `fr`
* `type` the requested type of entities like `Person`, `Place` or `Property`
* `limit` the number of query results to return. This value should be lower than 1000 and the default value is 100.
* `continue` allows to retrieve more results using a pagination system.

Example: `/search/simple?query=John&lang=fr&type=Person&limit100`

## Install

This service depends on Java 8 and some maven dependencies. To run it you should have Java 8 and maven installed on your machine then in the root directory of the project run the command:
```
mvn exec:java
```

The service will run and fill its database with the latest Wikidata dumps.

### Configuration

The main configuration file is `src/main/resources/config.properties`.

The following configuration variables are available:

* `us.askplatyp.kb.lucene.http.uri`: the server base URI. By default `http://localhost:4567`.
* `us.askplatyp.kb.lucene.lucene.directory`: the directory where Lucene should store its files. By default the relative directory `data`.
* `us.askplatyp.kb.lucene.wikidata.directory` directory where to store a `dumpfiles` directory containing downloaded Wikidata dumps. Default value : `user.dir` configuration variable.
