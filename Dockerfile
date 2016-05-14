#Server
FROM java
#RUN javac /rmi/*.java
COPY project2 /project2
#RUN javac /PongServer/*.java
EXPOSE "7000"
