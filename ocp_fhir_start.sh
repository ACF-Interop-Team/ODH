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
# Validate Enabler dependencies
# TBD

########### Inform the User of what we are using and what to Expect ############
# Validate that PosgreSQL is running
# TBD

# Inform the user of what we are using and what we are about to do
echo "OCP_DEV_HOME = ${OCP_DEV_HOME}"


############## Start the Services ######################


# Start HAPI FHIR Server
SERVICE_NAME="hapi-fhir-jpaserver-starter"
SERVICE_LOG_FILE=${OCP_DEV_HOME}/${SERVICE_NAME}.log
cd ${OCP_DEV_HOME}/ehnglobal-ws/${SERVICE_NAME}
pwd > ${SERVICE_LOG_FILE} # this will start a new log file
sdk use java 11.0.2-open
echo "Building the HAPI FHIR project  (${SERVICE_NAME})"
mvn clean install -X -Dmaven.test.skip >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
echo "Starting HAPI FHIR Server (${SERVICE_NAME})"
mvn spring-boot:run -Pboot >> ${SERVICE_LOG_FILE} 2>&1 & # this will append to the log file (or create it if not available)
echo "Started HAPI FHIR Server (${SERVICE_NAME})"

exit
