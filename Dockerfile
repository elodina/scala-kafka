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

FROM stealthly/docker-java

ENV REGISTRY_VERSION 1.0
ENV SCALA_VERSION 2.10.4
ENV REGISTRY_URL http://packages.confluent.io/archive/$REGISTRY_VERSION/confluent-$REGISTRY_VERSION-$SCALA_VERSION.tar.gz -O /tmp/registry.tgz
ENV REGISTRY_HOME /opt/confluent-$REGISTRY_VERSION

RUN wget -q $REGISTRY_URL -O /tmp/confluent-$REGISTRY_VERSION-$SCALA_VERSION.tgz
RUN tar xfz /tmp/confluent-$REGISTRY_VERSION-$SCALA_VERSION.tgz -C /opt

EXPOSE 8081

CMD $REGISTRY_HOME/bin/schema-registry-start $REGISTRY_HOME/etc/schema-registry/schema-registry.properties
