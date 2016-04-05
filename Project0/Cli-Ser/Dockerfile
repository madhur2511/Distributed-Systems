FROM java
MAINTAINER MadhurKapoor
RUN apt-get update && apt-get install -y vim

COPY CatServer.java /
COPY CatClient.java /

RUN javac CatServer.java
RUN javac CatClient.java
