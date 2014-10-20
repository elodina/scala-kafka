#!/bin/sh

mkdir -p $GOPATH/src/github.com/stealthly
cd $GOPATH/src/github.com/stealthly

#this is awful but there is no way to go get a specific branch :(
#fetch with dependencies
go get github.com/stealthly/go-kafka
go get github.com/stealthly/go-avro/decoder
rm -rf go-kafka

#switch to our branch
git clone https://github.com/stealthly/go-kafka.git --branch BDOS-6
cd $GOPATH/src/github.com/stealthly/go-kafka
go run scala_go_kafka.go $1 $2