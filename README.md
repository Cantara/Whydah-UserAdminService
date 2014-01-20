UserAdminService
====================

The optional component to allow 3rd party authenticated applications to add and manage users in Whydah IAM/SSO


![Architectural Overview](https://raw2.github.com/altran/Whydah-SSOWebApplication/master/Whydah%20infrastructure.png)



Installation
============



* create a user for the service
* create start_service.sh

```
#!/bin/sh

export IAM_MODE=TEST

A=UserAdminService
V=1.0-SNAPSHOT
JARFILE=$A-$V.jar

pkill -f $A

wget --user=altran --password=l1nkSys -O $JARFILE "http://mvnrepo.cantara.no/service/local/artifact/maven/content?r=altran-snapshots&g=net.whydah.sso.service&a=$A&v=$V&p=jar"
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
                ProxyPass /tokenservice http://localhost:9998/tokenservice
                ProxyPass /useradminservice http://localhost:9992/uas
</VirtualHost>
```
