#!/bin/sh

go get github.com/tools/godep
mkdir -p $GOPATH/src/github.com/stealthly
cd $GOPATH/src/github.com/stealthly
git clone https://github.com/stealthly/go-kafka.git
cd $GOPATH/src/github.com/stealthly/go-kafka
godep restore
go run scala_go_kafka.go $1 $2