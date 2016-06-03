FROM sequenceiq/hadoop-docker:2.7.0
MAINTAINER Amit Borase <aborase@eng.ucsd.edu>
RUN mkdir /data
COPY files/* /data/

RUN mkdir /usr/local/hadoop/dfs && \
mkdir /usr/local/hadoop/dfs/name && \
mkdir /usr/local/hadoop/dfs/name/data

# RUN /bin/bash
RUN echo "export PATH=$PATH:$HADOOP_PREFIX/bin" > /root/.bashrc

RUN mv /data/core-site.xml /usr/local/hadoop/etc/hadoop/core-site.xml && \
mv /data/hdfs-site.xml /usr/local/hadoop/etc/hadoop/hdfs-site.xml && \
mv /data/mapred-site.xml /usr/local/hadoop/etc/hadoop/mapred-site.xml && \
mv /data/hadoop-env.sh /usr/local/hadoop/etc/hadoop/hadoop-env.sh

RUN chmod +x /data/start-slave.sh
