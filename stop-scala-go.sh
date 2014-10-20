#!/bin/sh

docker kill scala
docker kill golang
docker kill broker1
docker kill zkserver

docker rm scala
docker rm golang
docker rm broker1
docker rm zkserver