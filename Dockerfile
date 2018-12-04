FROM openjdk:11-jre-slim

ENV PORT 4567
EXPOSE $PORT
ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/platypus-kb-lucene/platypus-kb-lucene.jar", "-Djdk.xml.entityExpansionLimit=2147480000"]

ADD target/lib /usr/share/platypus-kb-lucene/lib

ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/platypus-kb-lucene/platypus-kb-lucene.jar
