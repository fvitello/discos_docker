#! /bin/bash

mkdir -p results
rm -f results/*

failures=

###
### Test 1
###
wget --ignore-length "http://www.eso.org/projects/alma/AcsWebStart/latest/jnlp.html?file=livetest/AvailabilityTest.jnlp&host=localhost&acsinstance=0" -O results/AvailabilityTest1.wget 2>results/AvailabilityTest1.log

diff AvailabilityTest.ref results/AvailabilityTest1.wget > results/AvailabilityTest1.diff
if [ ! $? == 0 ] ; then
   echo "Test of Indirect (Listbox) style failed" 1>&2
   failures=true
fi


###
### Test 2
###
wget --ignore-length "http://www.eso.org/projects/alma/AcsWebStart/latest/livetest/AvailabilityTest.jnlp" -O results/AvailabilityTest2.wget 2>results/AvailabilityTest2.log

diff AvailabilityTest.ref results/AvailabilityTest2.wget > results/AvailabilityTest2.diff 
if [ ! $? == 0 ] ; then
   echo "Test of Direct (Html-Link) style failed" 1>&2
   failures=true
fi


###
### Results
###
if [ $failures ] ; then
   exit 1
fi


exit 0
