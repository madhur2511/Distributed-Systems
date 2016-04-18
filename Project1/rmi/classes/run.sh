cd ..
javac -d "classes" *.java Helper/*.java
cd classes
java rmi.TestServer
