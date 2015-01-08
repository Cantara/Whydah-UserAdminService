UserAdminService
====================

The component to allow 3rd party authenticated applications to add and manage users in Whydah IAM/SSO


![Architectural Overview](https://raw2.github.com/altran/Whydah-SSOLoginWebApp/master/Whydah%20infrastructure.png)


Installation
============


* create a user for the service
* run start_service.sh
* ..or use the ansible provisioning project to create a Whydah installation
* ..or create the files from info below:


```
#!/bin/sh

export IAM_MODE=TEST

A=UserAdminService
V=2.0.1.Final
JARFILE=$A-$V.jar

pkill -f $A


wget  -O $JARFILE "http://mvnrepo.cantara.no/service/local/artifact/maven/content?r=releases&g=net.whydah.identity&a=$A&v=$V&p=jar"
nohup java -jar -DIAM_CONFIG=useradminservice.TEST.properties $JARFILE &


tail -f nohup.out
```

* create useradminservice.TEST.properties

```
mybaseuri=http://xxxxx.cloudapp.net/useradminservice
service.port=9992
useridbackendUri=http://xxxxx.cloudapp.net/uib
testpage=false
```


Typical apache setup
====================

```
<VirtualHost *:443>
        ServerName myserver.net
        ServerAlias myserver
        ProxyRequests Off
        <Proxy *>
                Order deny,allow
                Allow from all
        </Proxy>
        ProxyPreserveHost on
                ProxyPass /useradminservice http://localhost:9992/uas
</VirtualHost>
```

## LICENSE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.