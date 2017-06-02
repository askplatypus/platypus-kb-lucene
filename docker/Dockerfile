FROM openjdk:8

ENV PORT 4567
EXPOSE $PORT

COPY  *.jar /

CMD java -jar '/kb-lucene-0.1-SNAPSHOT.jar' -Xmx16g -Djdk.xml.entityExpansionLimit=2147480000
