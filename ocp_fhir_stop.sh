#!/bin/bash

############## Setup the Enviroment ######################
# Set the OCP development directory
# ToDO: we can figure this out or source from the env
OCP_DEV_HOME=~/dev/src/ocp-laptop

# Source the user shell enviroment
source ~/.bash_profile

# Exit script if we encounter any errors
set -e

############## Validate the Enviroment ######################


############## Stop the Services ######################
# See if the server is running
ps -ef | grep -i java | grep -i hapi | cut -d \t -f 1-6

# Stop the HAPI FHIR Server
cd ${OCP_DEV_HOME}/ehnglobal-ws/hapi-fhir-jpaserver-starter
sdk use java 11.0.2-open
echo "Stopping HAPI FHIR Server"
mvn spring-boot:stop
echo "Stoped HAPI FHIR Server"

# Validate that the server is no longer running
ps -ef | grep -i java | grep -i hapi | cut -d \t -f 1-6

echo "Use the kill command if the process did not stop"

exit
