cd ..
javac -d "classes" *.java Helper/*.java
cd classes
java -Djava.util.logging.config.file=../logging.properties rmi.TestServer
