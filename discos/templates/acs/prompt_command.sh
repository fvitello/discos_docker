#! /usr/bin/env bash

if [ -f $HOME/bin/_discos-check-branch ]; then
	CHECK_OUTPUT=$($HOME/bin/_discos-check-branch)
	if [ -n "${CHECK_OUTPUT}" ]; then
		echo -e "${pur}WARNING:${txtrst} $CHECK_OUTPUT"
        alias make="
        echo -e \"${pur}WARNING:${txtrst} the 'make' command has been disabled in order to avoid messing up the default introot!\"
        echo \"         Set your desired branch using the 'discos-set' command before compiling and installing!\""
    else
        unalias make 2>/dev/null
	fi
fi

# Load DISCOS environment
# =======================
if [ -f /discos-sw/config/misc/load_branch ]; then
    source /discos-sw/config/misc/load_branch
fi

# Load ACS definitions
# ====================
if [ -f /discos-sw/config/acs/.bash_profile.acs ]; then
    source /discos-sw/config/acs/.bash_profile.acs
fi

export ACS_TMP=/service/acstmp/discos-manager

if [ -n "${DISCOS_BRANCH}" ]; then
    if [ -f $INTROOT/.station ]; then
        source $INTROOT/.station
    fi

    if [ $CDB = "test" ]; then
        export ACS_CDB=/discos-sw/discos/$DISCOS_BRANCH/$STATION
    else
        export ACS_CDB=/discos-sw/discos/$DISCOS_BRANCH/$STATION/Configuration
    fi
else
    if [ ! -f /discos-sw/config/misc/station ]; then
        unset STATION
    fi
fi

# Set the prompt
# ==============
red='\e[1;31m'  # Red
grn='\e[1;32m'  # Green
cyn='\e[1;36m'  # Cyan
pur='\e[1;35m'  # Purple
txtrst='\e[0m'  # Text Reset
if [ -n "${DISCOS_BRANCH}" ]; then
    PS1="(\[$grn\]$DISCOS_BRANCH\[$txtrst\]:\[$cyn\]$CDB\[$txtrst\]) \u@\h \w $ "
else
    PS1="(\[$red\]branch?\[$txtrst\]) \u@\h \w $ "
fi
