#!/bin/bash
N=4
input=$1

[ $# -eq 0 ] && { echo "USAGE: ./run.sh <relative-path-to-input-file>"; exit 1; }

cp -f $input hadoop-master/files/input.txt

cp -f BigramCounter.jar hadoop-master/files/BigramCounter.jar

docker rm -f master

i=1
rm -f hadoop-master/files/slaves
while [ $i -le $N ]
do
    echo slave$i >> hadoop-master/files/slaves
	docker rm -f slave$i
	i=$(( $i + 1 ))
done

docker rmi -f hadoop-slave
docker rmi -f hadoop-master
docker network rm hadoop-net

docker network create -d bridge hadoop-net

docker build -t hadoop-master ./hadoop-master/

docker build -t hadoop-slave ./hadoop-slave/

docker run -itd --net=hadoop-net --name master -h master hadoop-master bash

i=1
while [ $i -le $N ]
do
    docker run -itd --net=hadoop-net --name slave$i -h slave$i hadoop-slave bash
	i=$(( $i + 1 ))
done

docker exec -it master /data/start-cluster.sh
sleep 10

i=1
while [ $i -le $N ]
do
    docker exec -it slave$i /data/start-slave.sh
	i=$(( $i + 1 ))
done

sleep 30
docker exec -it master /data/run-bigram.sh
