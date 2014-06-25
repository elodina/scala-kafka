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
version=0.8.1.1
scala=2.10
release=kafka_$scala-$version

url=archive.apache.org/dist/kafka
wget https://$url/$version/$release.tgz
wget https://$url/$version/$release.tgz.md5
wget https://$url/$version/$release.tgz.sh1
wget https://$url/$version/$release.tgz.sh2
wget https://$url/$version/$release.tgz.asc
tar -xvf $release.tgz
/vagrant/vagrant/verify.sh $release.tgz
ln -s /opt/apache/$release kafka
exitscript