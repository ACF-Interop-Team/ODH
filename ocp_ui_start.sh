#!/bin/bash

#!/bin/bash


############## Setup the Enviroment ######################
# Set the OCP development directory
# ToDO: we can figure this out or source from the env
OCP_DEV_HOME=~/dev/src/ocp-laptop

# Source the user shell enviroment
source ~/.bash_profile

# Exit script if we encounter any errors
set +e


############## Validate the Enviroment ######################
# Validate Enabler dependencies
# TBD


########### Inform the User of what we are using and what to Expect ############
# Validate that PosgreSQL is running
# TBD

# Inform the user of what we are using and what we are about to do
echo "OCP_DEV_HOME = ${OCP_DEV_HOME}"


############## Start the Services ######################

SERVICE_NAME="ocp-ui"
SERVICE_LOG_FILE=${OCP_DEV_HOME}/${SERVICE_NAME}.log
cd ${OCP_DEV_HOME}/ehnglobal-ws/${SERVICE_NAME}
pwd > ${SERVICE_LOG_FILE} # this will start a new log file


echo "Setting the version of gradel in the wrapper for the project"
nvm -v >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
nvm install 17 >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
nvm use 17 >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
node -v >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)

echo "Starting Server (${SERVICE_NAME})"
npm rebuild  >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
npm start  >> ${SERVICE_LOG_FILE} 2>&1 & # this will append to the log file (or create it if not available)
echo "Started Server (${SERVICE_NAME})"

exit




cd $HOME/workspace/ehnglobal-ws/ocp-ui





exit
