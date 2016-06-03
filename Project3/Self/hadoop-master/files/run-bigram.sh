#!/bin/bash
export PATH=$PATH:$HADOOP_PREFIX/bin

cd $HADOOP_PREFIX

# create input directory and copy input files
mkdir /root/input
cp -rf /data/input.txt /root/input/input.txt

echo -e "\n Copying input file into HDFS"

# create input directory on HDFS
hadoop fs -mkdir -p input >/dev/null

# put input files to HDFS
hdfs dfs -put /root/input/* input >/dev/null

echo -e "\n Running bigram map-reduce phase"
# bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.0.jar grep input output 'dfs[a-z.]+'
bin/hadoop jar /data/BigramCounter.jar BigramCounter input output >/dev/null

# hdfs dfs -cat output/part-r-00000 > /data/output
hadoop fs -getmerge output/ /data/output >/dev/null

echo -e "\n Bigrams Output:"
bin/hadoop jar /data/BigramCounter.jar PostProcessing /data/output
