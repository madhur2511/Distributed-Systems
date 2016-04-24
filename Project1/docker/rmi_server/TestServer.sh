#!/bin/sh

cd ./classes
#java rmi.TestServer
java -Djava.util.logging.config.file=/logging.properties rmi.PingServerFactory
