#!/bin/sh

export DISPLAY=pc011824:2.0
. $HOME/.bashrc

# Run BuildDistro.py for all machines/distributions to build
/acs_build/src/BuildDistro.py -a &
