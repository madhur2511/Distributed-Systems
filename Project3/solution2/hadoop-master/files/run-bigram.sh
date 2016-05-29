#!/bin/bash
export PATH=$PATH:$HADOOP_PREFIX/bin

cd $HADOOP_PREFIX

# create input directory and copy input files
mkdir /root/input
cp -rf /data/input.txt /root/input/input.txt

# create input directory on HDFS
hadoop fs -mkdir -p input

# put input files to HDFS
hdfs dfs -put /root/input/* input

# Run bigram counter
# bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.0.jar grep input output 'dfs[a-z.]+'
bin/hadoop jar /data/BigramCounter.jar BigramCounter input output

# print the input files
echo -e "\ninput input.txt:"
hdfs dfs -cat input/input.txt

# print the output of wordcount
echo -e "\nwordcount output:"
hdfs dfs -cat output/part-r-00000 > /data/output

bin/hadoop jar /data/BigramCounter.jar PostProcessing /data/output
