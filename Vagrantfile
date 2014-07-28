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
# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "zkbk"

  # The url from where the 'config.vm.box' box will be fetched if it
  # doesn't already exist on the user's system.
  config.vm.box_url = "https://s3.amazonaws.com/stealthly_vagrantbox/zkbk.box"

  config.vm.define "zkbkOne" do |zkbkOne|
    zkbkOne.vm.network :private_network, ip: "192.168.30.1"
    zkbkOne.vm.provider :virtualbox do |vb|
      vb.customize ["modifyvm", :id, "--memory", "512"]
    end
    #zkbkOne.vm.provision "shell", path: "vagrant/init.sh"
  end

  config.vm.define "zkbkTwo" do |zkbkTwo|
    zkbkTwo.vm.network :private_network, ip: "192.168.30.2"
    zkbkTwo.vm.provider :virtualbox do |vb|
      vb.customize ["modifyvm", :id, "--memory", "512"]
    end
    #zkbkTwo.vm.provision "shell", path: "vagrant/init.sh", :args => "1"
  end

  config.vm.define "zkbkThree" do |zkbkThree|
    zkbkThree.vm.network :private_network, ip: "192.168.30.3"
    zkbkThree.vm.provider :virtualbox do |vb|
      vb.customize ["modifyvm", :id, "--memory", "512"]
    end
    #zkbkThree.vm.provision "shell", path: "vagrant/init.sh", :args => "1"
  end  
end
