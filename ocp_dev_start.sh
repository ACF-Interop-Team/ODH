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


sdk use java 8.312.07.1-amzn

# 'ocp-discovery-server' 'ocp-config-server' 'ocp-edge-server' 'ocp-fis' 'ocp-mint-api'  'ocp-ui-api' 'smart-core' 'smart-gateway'
declare -a OCP_SERVICES=('ocp-discovery-server' 'ocp-config-server' 'ocp-edge-server' 'ocp-fis' 'ocp-mint-api' 'ocp-ui-api' 'smart-core' 'smart-gateway');
for SERVICE_NAME in "${OCP_SERVICES[@]}"; do

  SERVICE_LOG_FILE=${OCP_DEV_HOME}/${SERVICE_NAME}.log
  cd ${OCP_DEV_HOME}/ehnglobal-ws/${SERVICE_NAME}/${SERVICE_NAME}
  pwd > ${SERVICE_LOG_FILE} # this will start a new log file
  echo "Building Server project (${SERVICE_NAME})"
  mvn clean install >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
  echo "Starting Server (${SERVICE_NAME})"
  mvn spring-boot:run >> ${SERVICE_LOG_FILE} 2>&1 & # this will append to the log file (or create it if not available)
  echo "Started Server (${SERVICE_NAME})"

done

exit

####################################### END ####################################

# Start Discovery Server
SERVICE_NAME="ocp-discovery-server"
SERVICE_LOG_FILE=${OCP_DEV_HOME}/${SERVICE_NAME}.log
cd ${OCP_DEV_HOME}/ehnglobal-ws/${SERVICE_NAME}/${SERVICE_NAME}
pwd > ${SERVICE_LOG_FILE} # this will start a new log file
sdk use java 8.312.07.1-amzn
echo "Building the Discovery Service project (${SERVICE_NAME})"
mvn clean install >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
echo "Starting Discovery Server (${SERVICE_NAME})"
mvn spring-boot:start >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
echo "Started Discovery Server (${SERVICE_NAME})"

### Start Config Server
SERVICE_NAME="ocp-config-server"
SERVICE_LOG_FILE=${OCP_DEV_HOME}/${SERVICE_NAME}.log
cd ${OCP_DEV_HOME}/ehnglobal-ws/${SERVICE_NAME}/${SERVICE_NAME}
pwd > ${SERVICE_LOG_FILE} # this will start a new log file
sdk use java 8.312.07.1-amzn
echo "Building the Discovery Service project (${SERVICE_NAME})"
mvn clean install >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
echo "Starting Discovery Server (${SERVICE_NAME})"
mvn spring-boot:start >> ${SERVICE_LOG_FILE} 2>&1 # this will append to the log file (or create it if not available)
echo "Started Discovery Server (${SERVICE_NAME})"


exit



### Fis
1. Open a new terminal
```
cd $HOME/workspace/ehnglobal-ws/ocp-fis/ocp-fis
mvn clean install
mvn spring-boot:run
```

### Edge Server
1. Open a new terminal
```
cd $HOME/workspace/ehnglobal-ws/ocp-edge-server/ocp-edge-server
mvn clean install
mvn spring-boot:run



### Ui
1. Open a new terminal
```
cd $HOME/workspace/ehnglobal-ws/ocp-ui
npm install
npm start
```


### Ui api
1. Open a new terminal
```
cd $HOME/workspace/ehnglobal-ws/ocp-ui-api/ocp-ui-api
mvn clean install
mvn spring-boot:run
```

### Mint
1. Open a new terminal
```
cd $HOME/workspace/ehnglobal-ws/ocp-mint-api/ocp-mint-api
mvn clean install
mvn spring-boot:run
```

### Uaa
1. Open a new terminal
```
echo "Add external configuration to tomcat"
echo 'CLOUD_FOUNDRY_CONFIG_PATH="$HOME/workspace/ehnglobal-ws/ocp-uaa/external-configuration"' >> $HOME/.sdkman/candidates/tomcat/9.0.40/conf/catalina.properties
echo "Add environment variables"
export UAA_SMTP_HOST="smtp.gmail.com"
export UAA_SMTP_PASSWORD="P@ssword123%"
export UAA_SMTP_PORT="587"
export UAA_SMTP_USER="wastedprogramming@gmail.com"
echo "Move to uaa folder"
cd $HOME/workspace/ehnglobal-ws/ocp-uaa/
echo "Build the project"
gradle wrapper --gradle-version 3.3
./gradlew clean install
echo "Move war file to tomcat"
cp uaa/build/libs/cloudfoundry-identity-uaa-4.8.0-11.war $HOME/.sdkman/candidates/tomcat/9.0.40/webapps/uaa.war
echo "Adding execution right to catalina file"
chmod +x $HOME/.sdkman/candidates/tomcat/9.0.40/bin/catalina.sh
echo "Starting tomcat"
sudo sh $HOME/.sdkman/candidates/tomcat/9.0.40/bin/startup.sh
```
SHELL=/usr/local/bin/bash
XPC_FLAGS=0x0
JAVA_HOME$HOME/.sdkman/candidates/java/current
SSH_AUTH_SOCK=/private/tmp/com.apple.launchd.GjuHQEp9V7/Listeners
GRADLE_HOME=$HOME/.sdkman/candidates/gradle/current
JAVA_OPTS=-Xms256m -Xmx512m
SDKMAN_CANDIDATES_DIR=$HOME/.sdkman/candidates
PWD=$HOME/dev/src/ocp-laptop
LOGNAME=vic_o
HOME=$HOME
LANG=en_US.UTF-8
SDKMAN_VERSION=5.13.1
TMPDIR=/var/folders/p4/9fgxgjjs11vf7708t1h04fhw0000gn/T/
GROOVY_HOME=$HOME/.sdkman/candidates/groovy/current
TERM=xterm-256color
USER=vic_o
MAVEN_HOME=$HOME/.sdkman/candidates/maven/current
SDKMAN_DIR=$HOME/.sdkman
SHLVL=2
SDKMAN_CANDIDATES_API=https://api.sdkman.io/2
GRAILS_HOME=$HOME/.sdkman/candidates/grails/current
XPC_SERVICE_NAME=0
TOMCAT_HOME=$HOME/.sdkman/candidates/tomcat/current
PATH=$HOME/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:/Library/Apple/usr/bin:$HOME/dev/bin/google-cloud-sdk/bin:$HOME/.sdkman/candidates/tomcat/current/bin:$HOME/.sdkman/candidates/maven/current/bin:$HOME/.sdkman/candidates/java/current/bin:$HOME/.sdkman/candidates/groovy/current/bin:$HOME/.sdkman/candidates/grails/current/bin:$HOME/.sdkman/candidates/gradle/current/bin:$HOMED/bin
ORIGINAL_XDG_CURRENT_DESKTOP=undefined
SDKMAN_PLATFORM=darwinx64
__CF_USER_TEXT_ENCODING=0x1F5:0x0:0x0
TERM_PROGRAM=platformio-ide-terminal
_=/usr/bin/env
