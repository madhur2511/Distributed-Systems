#!/bin/sh

cd ../rmi
javac -d "classes" *.java
rm -rf ../docker/rmi_node/classes
cp -rf classes ../docker/rmi_node

docker rm -f rmiclient
docker rm -f rmiserver
docker rm -f rminode
docker rmi -f rmi_node
docker rmi -f rmi_server
docker rmi -f rmi_client
docker network rm mynet

docker network create -d bridge mynet

cd ../docker

docker build -t rmi_node ./rmi_node/
docker run -itd --name rminode rmi_node

docker build -t rmi_server ./rmi_server/
docker build -t rmi_client ./rmi_client/

docker run -itd --net=mynet --name rmiserver --volumes-from rminode rmi_server /bin/bash  -c "source /TestServer.sh"

sleep 5

docker run -itd --net=mynet --name rmiclient --volumes-from rminode rmi_client /bin/bash  -c "source /TestClient.sh"

sleep 5

echo "*******************
* RMI server logs *
*******************"
docker logs rmiserver
echo "*******************
* RMI client logs *
*******************"
docker logs rmiclient
