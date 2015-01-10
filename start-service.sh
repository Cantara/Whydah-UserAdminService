#!/bin/sh

nohup /usr/bin/java -DIAM_MODE=PROD -DIAM_CONFIG=/home/UserAdminService/useradminservice.PROD.properties -jar /home/UserAdminService/UserAdminService.jar &


