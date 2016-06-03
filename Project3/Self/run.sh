#!/bin/bash
N=4
input=$1

[ $# -eq 0 ] && { echo "USAGE: ./run.sh <relative-path-to-input-file>"; exit 1; }

echo -e "\n Copying JAR and input files to master-image prep"

cp -f $input hadoop-master/files/input.txt

cp -f BigramCounter.jar hadoop-master/files/BigramCounter.jar

echo -e "\n Removing past containers and images for Master and slave if any found"

docker rm -f master >/dev/null

i=1
rm -f hadoop-master/files/slaves
while [ $i -le $N ]
do
    echo slave$i >> hadoop-master/files/slaves
	docker rm -f slave$i >/dev/null
	i=$(( $i + 1 ))
done

docker rmi -f hadoop-slave >/dev/null
docker rmi -f hadoop-master >/dev/null

echo -e "\n Creating a new bridge network, hadoop-net"

docker network rm hadoop-net >/dev/null

docker network create -d bridge hadoop-net >/dev/null

echo -e "\n Building images for master and slave"

docker build -t hadoop-master ./hadoop-master/ >/dev/null

docker build -t hadoop-slave ./hadoop-slave/ >/dev/null

docker run -itd --net=hadoop-net --name master -h master hadoop-master bash >/dev/null

i=1
while [ $i -le $N ]
do
    docker run -itd --net=hadoop-net --name slave$i -h slave$i hadoop-slave bash >/dev/null
	i=$(( $i + 1 ))
done

echo -e "\n Instantiating master"

docker exec -it master /data/start-cluster.sh >/dev/null
sleep 10

echo -e "\n Instantiating slaves, please wait, it might take about a minute!"

i=1
while [ $i -le $N ]
do
    docker exec -it slave$i /data/start-slave.sh >/dev/null
	i=$(( $i + 1 ))
done

sleep 30

docker exec -it master /data/run-bigram.sh
