#!/bin/bash

cd Cli-Ser
docker build -t client-server:v1 .

cd ../DataVol
docker build -t data-volume:v1 .

# Create the Data Volume Container
docker create -v /data --name Vol data-volume:v1 /bin/true

# Instantiate the client-server image to start a container with server logic
docker run -itd -P --volumes-from Vol --name server client-server:v1 java CatServer "data/string.txt" 15000

# Instantiate the client-server image to start a container with client logic
docker run -it --volumes-from Vol --link server:CatServer --name client client-server:v1 java CatClient "data/string.txt" 15000

# Test whether MISSING is encountered in any logs made by the client; If so output -1 indicating Failure, else 0 indicating Success
res=0
docker logs client | while read -r line ; do
    if [ "$line" = "MISSING" ]
    then res=-1
    fi
done
echo $res


# Clean Up
docker rm `docker ps -aq`
docker rmi client-server:v1
docker rmi data-volume:v1
