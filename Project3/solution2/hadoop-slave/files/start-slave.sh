#!/bin/bash
export PATH=$PATH:$HADOOP_PREFIX/bin

cd $HADOOP_PREFIX

sbin/hadoop-daemon.sh start datanode
