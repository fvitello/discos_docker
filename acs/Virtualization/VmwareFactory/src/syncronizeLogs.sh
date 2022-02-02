#!/bin/bash

declare REMOTE_HOST='websqa' 
declare REMOTE_USER='sqa-ops'
declare MAILTO='sturolla@eso.org'


PATH='/usr/bin:/bin'

declare -ar LOGDIR=(rhel4-acs5 rhel4-acs6 sl4-acs5 sl4-acs6)

declare STATUS_FILE 
declare LOG_DIR
declare LOCAL_DIR

# iterate on vm host names
#RSYNC='/usr/bin/rsync -q -av '
RSYNC='/usr/bin/rsync -av '
for name in ${LOGDIR[*]};
do
        STATUS_FILE="/home/web/sqa/docs/alma/snapshotVM$name/logs/status.html"
        LOG_DIR="/home/web/sqa/docs/alma/snapshotVM$name/logs/"
        LOCAL_DIR="/acs_build/log/$name"
        echo "Executing: $RSYNC $LOCAL_DIR/* $REMOTE_USER@$REMOTE_HOST:$LOG_DIR"
        $RSYNC $LOCAL_DIR/* $REMOTE_USER@$REMOTE_HOST:$LOG_DIR
	echo " "
done
