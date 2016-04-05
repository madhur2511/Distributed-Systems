#!/bin/bash

# # Creating a Docker Host machine
# echo "Creating a host machine to run the Docker daemon and engine"
# docker-machine create --driver virtualbox default1
#
# echo "$(docker-machine env default1)" | \
# { while read -r line
#     do
#         eval `echo $line`
#     done
# }

#-----------------------------------------------------------------------------------------------

echo "\nBuilding image for client-server from the local context with java files"
cd Cli-Ser
docker build -t client-server:v1 . >/dev/null

echo "Building image for Data volume container, copying the shared string.txt file\n\n"
cd ../DataVol
docker build -t data-volume:v1 . >/dev/null

#-----------------------------------------------------------------------------------------------

# Create the Data Volume Container
echo "Initializing the Data Volume container from the image"
docker create -v /data --name Vol data-volume:v1 /bin/true >/dev/null

# Instantiate the client-server image to start a container with server logic
echo "Initializing the Server container from the common client-server image"
docker run -itd -P --volumes-from Vol --name server client-server:v1 java CatServer "/data/string.txt" 15000 >/dev/null

# Instantiate the client-server image to start a container with client logic
echo "Initializing the Client container from the common client-server image\n\n"
docker run -it --volumes-from Vol --link server:CatServer --name client client-server:v1 java CatClient "/data/string.txt" 15000 >/dev/null

#-----------------------------------------------------------------------------------------------

#Test whether MISSING is encountered in any logs made by the client; If so output -1 indicating Failure, else 0 indicating Success

echo "Testing the logs for the client, -1 denotes Failure; 0 denotes Success"
res=0
docker logs client |
{
   while read -r line ; do
       if [[ $line == *"MISSING"* ]]
       then
       res=-1
       fi
       echo $line
   done
echo "THE OVERALL RESULT OF THE TEST IS: $res\n\n"
}

#-----------------------------------------------------------------------------------------------

# Clean Up Docker containers and images
echo "Cleaning up after the tests, removing containers and images"
docker rm `docker ps -aq` >/dev/null
docker rmi client-server:v1 >/dev/null
docker rmi data-volume:v1 >/dev/null

# Stop and Clean up Docker-Machine called Default1
# echo "Cleaning up the Docker host machine created\n"
# docker-machine stop default1
# docker-machine rm -y default1
