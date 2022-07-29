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

export UAA_SMTP_HOST="smtp.gmail.com"
export UAA_SMTP_PASSWORD="P@ssword123%"
export UAA_SMTP_PORT="587"
export UAA_SMTP_USER="wastedprogramming@gmail.com"




########### Inform the User of what we are using and what to Expect ############
# Validate that PosgreSQL is running
# TBD

# Inform the user of what we are using and what we are about to do
echo "OCP_DEV_HOME = ${OCP_DEV_HOME}"


############## Start the Services ######################

sdk use java 8.312.07.1-amzn

SERVICE_NAME="ocp-uaa"
SERVICE_LOG_FILE=${OCP_DEV_HOME}/${SERVICE_NAME}.log
cd ${OCP_DEV_HOME}/ehnglobal-ws/${SERVICE_NAME}
pwd > ${SERVICE_LOG_FILE} # this will start a new log file
echo "Setting the version of gradel in the wrapper for the project"
gradle wrapper --gradle-version 3.3 >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
echo gradle -version
echo "Starting Server (${SERVICE_NAME})"
./gradlew clean run >> ${SERVICE_LOG_FILE} 2>&1 & # this will append to the log file (or create it if not available)
echo "Started Server (${SERVICE_NAME})"



exit
