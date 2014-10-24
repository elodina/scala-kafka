scala-kafka
===========

Quick up and running using Scala for Apache Kafka

Use Vagrant to get up and running.

1) Install Vagrant [http://www.vagrantup.com/](http://www.vagrantup.com/)  
2) Install Virtual Box [https://www.virtualbox.org/](https://www.virtualbox.org/)  

In the main kafka folder  

1) vagrant up  
2) ./gradlew test

once this is done 
* Zookeeper will be running 192.168.86.5
* Broker 1 on 192.168.86.10
* All the tests in src/test/scala/* should pass  

If you want you can login to the machines using vagrant ssh <machineName> but you don't need to.

You can access the brokers and zookeeper by their IP from your local without having to go into vm.

e.g.

bin/kafka-console-producer.sh --broker-list 192.168.86.10:9092 --topic <get his from the random topic created in test>

bin/kafka-console-consumer.sh --zookeeper 192.168.86.5:2181 --topic <get his from the random topic created in test> --from-beginning

scala-go-kafka
===========

Scala-kafka and go-kafka working together

Install Docker [https://docs.docker.com/installation/#installation](https://docs.docker.com/installation/#installation)  

In the main scala-kafka folder  

1) ./gradlew scalago jar    
2) ./start-scala-go.sh
3) ./stop-scala-go.sh to stop