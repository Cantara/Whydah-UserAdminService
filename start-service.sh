#!/bin/sh

#  If IAM_MODE not set, use PROD
if [ -z "$IAM_MODE" ]; then 
  IAM_MODE=PROD
fi

# If IAM_CONFOG not set, use PROD properties from local directory
if [ -z "$IAM_CONFIG" ]; then 
  IAM_CONFIG=useradminservice.PROD.properties
fi

# If Version is from source, find the artifact
if [ $Version = "FROM_SOURCE" ]; then 
    # Find the bult artifact
    Version=$(find target/* -name *.jar | grep SNAPSHOT | grep -v original | grep -v lib)
else
    Version=UserAdminService.jar
fi



