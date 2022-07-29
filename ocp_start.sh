
#!/bin/bash

############## Setup the Enviroment ######################
# Set the OCP development directory
# ToDO: we can figure this out or source from the env
OCP_DEV_HOME=~/dev/src/ocp-laptop

#${OCP_DEV_HOME}/ocp_db_start.sh
${OCP_DEV_HOME}/ocp_uaa_start.sh
${OCP_DEV_HOME}/ocp_fhir_start.sh
${OCP_DEV_HOME}/ocp_dev_start.sh
${OCP_DEV_HOME}/ocp_ui_start.sh

exit
