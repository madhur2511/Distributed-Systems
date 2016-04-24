#!/bin/sh

cd ./classes
#java rmi.TestClient
java -Djava.util.logging.config.file=/logging.properties rmi.PingPongClient
