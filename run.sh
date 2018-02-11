#!/bin/bash

echo "start compiling $(date)"
cd src
sbt assembly
echo "compiling $(date)"

echo "start running $(date)"
cd ..
java -jar src/target/scala-2.12/donation_analytics.jar
echo "done $(date)"
