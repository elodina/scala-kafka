#!/bin/sh

ZOOKEEPER=`docker ps -a | awk '{print $NF}'  | grep "zookeeper$"`
ZOOKEEPER_RUNNING=$?
if [ $ZOOKEEPER_RUNNING -eq 0 ] ;
then
    echo "Zookeeper is already running"
else
    echo "Starting Zookeeper"
    docker run --net=host --name zkserver -d stealthly/docker-zookeeper
fi

ID=1
PORT=9092
HOST_IP=localhost

docker run --net=host --name=broker$ID -p $PORT:$PORT -e BROKER_ID=$ID -e HOST_IP=$HOST_IP -e PORT=$PORT -d stealthly/docker-kafka

#let kafka initialize properly before establishing producers and consumers
sleep 10
go_topic=`head -c 100 /dev/urandom | base64 | sed 's/[+=/A-Z]//g' | tail -c 16`
scala_topic=`head -c 100 /dev/urandom | base64 | sed 's/[+=/A-Z]//g' | tail -c 16`

docker run --name scala --net=host -v $(pwd)/build/libs:/scala-kafka/build/libs -d stealthly/docker-java java -jar /scala-kafka/build/libs/scala-kafka-0.1.0.0.jar $scala_topic $go_topic
docker run --name golang --net=host -v $(pwd):/go-files -d golang:1.3.0 /go-files/run-go.sh $go_topic $scala_topic

# output logs to stdout
docker logs -f scala & docker logs -f golang &