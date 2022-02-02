#!/bin/bash

global_result=">>>>>PASSED"
#Lets activate the receiver 
#with error flag set
#an being active for 15 seconds
bulkDataNTGenReceiver   -s TestStream -f00,02,03 -e -t 15 &> $ACS_TMP/receiver.log  &
sleep 3
#lets activate the sender with one loop
bulkDataNTGenSender -l 3 -s TestStream -f00,01,03 -b 40000000 -n &> $ACS_TMP/sender.log  &
sleep 25 

is_text_in_file () {
    if  grep -Fq "$1" $2; then
       echo "$3-$1 PASSED"
    else
       echo "$3-$1 ERROR" 
       global_result=">>>>>FAILED"
    fi
}
#receiver test
echo   "$(is_text_in_file 'Error flag set' $ACS_TMP/receiver.log 'GenReceiver' )"
echo   "$(is_text_in_file 'errorStatusSupplier_p created' $ACS_TMP/receiver.log 'GenReceiver')  "
echo   "$(is_text_in_file 'got stop (BD_STOP)' $ACS_TMP/receiver.log 'GenReceiver') "
echo   "$(is_text_in_file 'TestStream has been destroyed' $ACS_TMP/receiver.log 'GenReceiver') "

#sender test


echo  $(is_text_in_file 'errorStatusConsumer_p->consumerReady()' $ACS_TMP/sender.log 'GenSender' ) 
echo  $(is_text_in_file 'I got a BAD_RECEIVER event' $ACS_TMP/sender.log 'GenSender') 
echo  $(is_text_in_file 'Destroyed the thread responsible for checking the status of the Notify Channel BD_ERR_PROP' $ACS_TMP/sender.log 'GenSender') 
echo  $(is_text_in_file 'TestStream has been destroyed' $ACS_TMP/sender.log 'GenSender')
               
#final result
echo "$global_result"
