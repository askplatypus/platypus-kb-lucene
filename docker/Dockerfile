FROM openjdk:9-jre-slim

ENV PORT 4567
EXPOSE $PORT

COPY  *.jar /

CMD java -jar '/kb-lucene-0.1-SNAPSHOT.jar' -Djdk.xml.entityExpansionLimit=2147480000
