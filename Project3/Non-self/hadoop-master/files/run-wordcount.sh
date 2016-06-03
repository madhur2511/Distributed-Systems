#!/bin/bash

# test the hadoop cluster by running wordcount

# create input files
mkdir input
cp -rf /tmp/input.txt input/input.txt
# echo "Hello Docker Hi How are you Hello Docker Good Night How are Hi are" >input/file2.txt
# echo "Hello Hadoop Docker hello How are " >input/file1.txt

# create input directory on HDFS
/usr/local/hadoop/bin/hadoop fs -mkdir -p input

# put input files to HDFS
/usr/local/hadoop/bin/hdfs dfs -put ./input/* input

# run wordcount
# hadoop jar $HADOOP_INSTALL/share/hadoop/mapreduce/sources/hadoop-mapreduce-examples-2.3.0-sources.jar org.apache.hadoop.examples.WordCount input output
/usr/local/hadoop/bin/hadoop jar /tmp/BigramCounter.jar BigramCounter input output

# print the input files
echo -e "\ninput input.txt:"
/usr/local/hadoop/bin/hdfs dfs -cat input/input.txt

# echo -e "\ninput file2.txt:"
# hdfs dfs -cat input/file2.txt

# print the output of wordcount
echo -e "\nwordcount output:"
/usr/local/hadoop/bin/hdfs dfs -cat output/part-r-00000 > /tmp/output

/usr/local/hadoop/bin/hadoop jar /tmp/BigramCounter.jar PostProcessing /tmp/output
