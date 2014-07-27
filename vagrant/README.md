# Apache Kafka #

Using Vagrant to get up and running.

1) Install Vagrant [http://www.vagrantup.com/](http://www.vagrantup.com/)  
2) Install Virtual Box [https://www.virtualbox.org/](https://www.virtualbox.org/)  

    vagrant up --provider=virtualbox

```
sudo su
cd /vagrant/vagrant
./up.sh

cd /opt/apache/kafka && bin/zookeeper-server-start.sh config/zookeeper.properties 1>>/tmp/zk.log 2>>/tmp/zk.log &
cd /opt/apache/kafka && bin/kafka-server-start.sh config/server.properties 1>>/tmp/bk.log 2>>/tmp/bk.log &

bin/kafka-topics.sh --create -topic test1 --replication-factor 3 --partitions 3 --zookeeper 192.168.30.1:2181,192.168.30.2:2181,192.168.3.30:2181

bin/kafka-topics.sh --describe --zookeeper 192.168.30.1:2181,192.168.30.2:2181,192.168.3.30:2181
Topic:test1  PartitionCount:3  ReplicationFactor:3  Configs:
  Topic: test1  Partition: 0  Leader: 1  Replicas: 1,3,2  Isr: 1,3,2
  Topic: test1  Partition: 1  Leader: 2  Replicas: 2,1,3  Isr: 2,1,3
  Topic: test1  Partition: 2  Leader: 3  Replicas: 3,2,1  Isr: 3,2,1


kill -9 `ps aux | grep Kafka | awk '{print $2}'`
```