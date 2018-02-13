#!/bin/bash

echo "start compiling $(date)"
cd src
sbt assembly
cd ..
echo "compiling done $(date)"

if [[ ! -f src/target/scala-2.12/donation_analytics.jar ]]
then
	echo "compiling failed? will use the pre-compiled jar file $(date)"
	java -Xmx4g -jar src/target/scala-2.12/donation_analytics_fallback.jar $@
else
	echo "start running $(date)"
	java -Xmx4g -jar src/target/scala-2.12/donation_analytics.jar $@
fi
echo "done $(date)"
