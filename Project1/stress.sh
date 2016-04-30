#!/bin/bash

runs=0
while [ $runs -lt 1000 ]
do
    runs=`expr $runs + 1`
    make test >> ./makeout
    sleep 1
done
