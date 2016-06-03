#!/bin/bash
export PATH=$PATH:$HADOOP_PREFIX/bin

echo "Y" | hdfs namenode -format

cd $HADOOP_PREFIX

sbin/start-dfs.sh && \
sbin/start-yarn.sh

sbin/hadoop-daemon.sh start namenode
