#!/bin/bash

export MAILTO='sturolla@eso.org'
PATH='/usr/bin:/bin'
REMOTE_HOST='websqa'
REMOTE_USER='sqa-ops'

STATUS_FILE1='/home/web/sqa/docs/alma/snapshotVM1/logs/status.html'
STATUS_FILE2='/home/web/sqa/docs/alma/snapshotVM2/logs/status.html'
LOG_DIR1='/home/web/sqa/docs/alma/snapshotVM1/logs/'
LOG_DIR2='/home/web/sqa/docs/alma/snapshotVM2/logs/'

LOCAL_DIR1='/acs_build/log/rhel4'
LOCAL_DIR2='/acs_build/log/sl4'

RSYNC='/usr/bin/rsync -q -av '
## Start Syncronising logs
#echo "============================================================="
#date
#echo "============================================================="
#env
#echo "============================================================="
$RSYNC $LOCAL_DIR1/* $REMOTE_USER@$REMOTE_HOST:$LOG_DIR1
$RSYNC $LOCAL_DIR2/* $REMOTE_USER@$REMOTE_HOST:$LOG_DIR2
