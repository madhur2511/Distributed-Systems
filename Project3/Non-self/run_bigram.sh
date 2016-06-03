#!/bin/bash

input=$1

[ $# -eq 0 ] && { echo "USAGE: ./run_bigram.sh <relative-path-to-input-file>"; exit 1; }

cp -f $input hadoop-master/files/input.txt

cp -f ../BigramCounter.jar hadoop-master/files/BigramCounter.jar

docker rm -f slave1
docker rm -f slave2
docker rm -f slave3
docker rm -f slave4
docker rm -f master

./build-image.sh hadoop-master

./build-image.sh hadoop-slave

./start-container.sh &

sleep 40

sudo docker exec -it master ./start-hadoop.sh

sleep 30

sudo docker exec -it master ./run-wordcount.sh
