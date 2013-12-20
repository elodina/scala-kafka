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

#!/bin/sh -Eu

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

echo "cd /tmp"
cd /tmp

archive="kafka_2.8.0-0.8.0.1-KAFKA-1180"

#echo "wget https://archive.apache.org/dist/kafka/0.8.0/kafka_2.8.0-0.8.0.tar.gz"
#wget https://archive.apache.org/dist/kafka/0.8.0/kafka_2.8.0-0.8.0.tar.gz
echo "wget http://people.apache.org/~joestein/KAFKA-1180/$archive.tar.gz"
wget http://people.apache.org/~joestein/KAFKA-1180/$archive.tar.gz

echo "tar -xvf $archive.tar.gz"
tar -xvf $archive.tar.gz

echo "mkdir -p /opt/apache"
mkdir -p /opt/apache

echo "mv $archive /opt/apache/kafka"
mv $archive /opt/apache/kafka

exitscript