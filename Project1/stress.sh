#!/bin/bash

runs=0
while [ $runs -lt 1000 ]
do
    runs=`expr $runs + 1`
    make test | grep -i "passed: "
done
