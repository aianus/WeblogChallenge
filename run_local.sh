#!/bin/bash

sbt assembly && \
spark-submit \
    --driver-memory 8g \
    --class com.alexianus.paytm_weblog_challenge.Solution \
    --master 'local[*]' \
    target/scala-2.11/paytm_weblog_challenge-assembly-1.0.jar \
    data/*.log.gz \
    2> error.log
