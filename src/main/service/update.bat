net stop SecurityTokenService
bin\wget -O SecurityTokenService-0.1-SNAPSHOT.jar "http://10.15.1.5:8080/nexus/service/local/artifact/maven/redirect?r=snapshots&g=net.whydah.identity&a=SecurityTokenService&v=0.1-SNAPSHOT&p=jar"
net start SecurityTokenService