# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#!/bin/sh -Eux

#  Trap non-normal exit signals: 1/HUP, 2/INT, 3/QUIT, 15/TERM, ERR
trap founderror 1 2 3 15 ERR

founderror()
{
        exit 1
}

exitscript()
{
        #remove lock file
        #rm $lockfile
        exit 0
}

mkdir -p /mnt/data/zookeeper
IP=$(ifconfig  | grep 'inet addr:'| grep 168 | grep 192|cut -d: -f2 | awk 'BEGIN { FS = "." } ; { print $4 }'|awk 'BEGIN { FS = " " } ; { print $1 }')

sed 's/MYID/'$IP'/' /vagrant/config/myid > /mnt/data/zookeeper/myid

cp /vagrant/config/zookeeper.properties /opt/apache/kafka/config/
cd /opt/apache/kafka


mkdir -p /mnt/data/kafka-logs

cp /vagrant/config/server.properties /opt/apache/kafka/config/
sed 's/MYID/'$IP'/' /vagrant/config/server.properties > /opt/apache/kafka/config/server.properties

cd /opt/apache/kafka

#cd /opt/apache/kafka && bin/zookeeper-server-start.sh config/zookeeper.properties 1>>/tmp/zk.log 2>>/tmp/zk.log &
#cd /opt/apache/kafka && bin/kafka-server-start.sh config/server.properties 1>>/tmp/bk.log 2>>/tmp/bk.log &

exitscript