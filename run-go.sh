#!/bin/sh

mkdir -p $GOPATH/src/github.com/stealthly
cd $GOPATH/src/github.com/stealthly
go get github.com/stealthly/go-kafka
cd $GOPATH/src/github.com/stealthly/go-kafka
go run scala_go_kafka.go $1 $2