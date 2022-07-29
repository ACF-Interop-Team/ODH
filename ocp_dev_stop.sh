#!/bin/bash



source ~/.bash_profile


OCP_DEV_HOME=~/dev/src/ocp-laptop


echo "OCP_DEV_HOME = ${OCP_DEV_HOME}"

# Exit script if we encounter any errors
#set -e

# See if the server is running
ps -ef | grep java | cut -d \t -f 1-6

sdk use java 8.312.07.1-amzn

# 'ocp-discovery-server' 'ocp-config-server' 'ocp-fis' 'ocp-edge-server' 'ocp-ui-api' 'ocp-mint-api'
declare -a OCP_SERVICES=('ocp-discovery-server' 'ocp-config-server' 'ocp-fis' 'ocp-edge-server' 'ocp-ui-api' 'ocp-mint-api');

for SERVICE_NAME in "${OCP_SERVICES[@]}"; do

  SERVICE_LOG_FILE=${OCP_DEV_HOME}/${SERVICE_NAME}.log
  cd ${OCP_DEV_HOME}/ehnglobal-ws/${SERVICE_NAME}/${SERVICE_NAME}
  pwd >> ${SERVICE_LOG_FILE} # this will start a new log file
  echo "Stopping Server ${SERVICE_NAME}"
  mvn spring-boot:stop >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
  echo "Started Server (${SERVICE_NAME})"

done

# Validate that the server is no longer running
ps -ef | grep java | grep -v grep | cut -d \t -f 1-6
echo "Use the kill command if the process did not stop for example..."
echo " ps -ef | grep java | grep -v grep | awk '{print \$2}' | xargs kill"

exit
