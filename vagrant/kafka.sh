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

mkdir -p /opt/apache
cd /opt/apache
wget http://people.apache.org/~joestein/kafka-0.8.1-candidate1/kafka_2.8.0-0.8.1.tgz #https://archive.apache.org/dist/kafka/0.8.0/kafka_2.8.0-0.8.0.tar.gz
mkdir -p /opt/apache/kafka_2.8.0-0.8.1
chmod a+rw /opt/apache/kafka_2.8.0-0.8.1
cd kafka_2.8.0-0.8.1
tar -xvf ../kafka_2.8.0-0.8.1.tgz
cd /opt/apache
ln -s /opt/apache/kafka_2.8.0-0.8.1 kafka
exitscript