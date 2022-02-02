#! /bin/bash

##
## author: mschilli 2007
##

THIS=`basename $0`
THIS_VAR="\$tomca_passwd"

###
### Help
###

if [ ! $1 ] || [ ! $2 ] || [ $1 = -h ] ; then
   echo "Simple tool to help managing Alma web applications"
   echo "Usage:"
   echo "   $THIS  install|reload|list  <appname>"
   echo "Examples:"
   echo "   $THIS list AcsWebStart"
   echo "   $THIS reload  AcsWebStart"
   echo "   $THIS reload  AcsWebStart/latest"
   echo "   $THIS install AcsWebStart/6.0"
   echo "Notes:"
   echo "   This will prompt for an http-password unless $THIS_VAR is defined"
   exit 2;
fi

###
### Arguments
###

command=$1
appname=$2

user="web"
passwd=`eval echo $THIS_VAR`
if [ ! $passwd ] ; then
   echo "$THIS: $THIS_VAR not defined, so enter http-passwd for user $user: "
   read passwd
fi


###
### Constants (valid from spring 2006)
###

manager="wget --no-verbose --output-document -  --http-user=$user --http-passwd=$passwd  http://arena:8280/tcmanager"
pathbase="/projects/alma/"
appbase="file:/home/web/server/apache/catalina.alma.arena/alma/"

path=$pathbase$appname  # Tomcat's "Context Path"
app=$appbase$appname    # Tomcat's "Directory URL"


###
### Functions
###

function install {
   $manager/install?path=$path\&war=$app
}

function reload {
   $manager/reload?path=$path
}

function list {
   $manager/list | grep $appname
}


$command
