#! /bin/bash

### msc(2005-05-31):
### At least one webstart version (1.0.2) cannot deal with signed jars
### where the manifest contains entries for which there's no corresponding
### class contained in the jar. Instead, webstart will fail with the terse
### message "Missing signed entry in resource" or similar.
### This occurs regulary with jars that were created/signed by some third-parties.
### This script recreates the jar manifest by recreating the jar.
###
### msc(2005-07-01):
### Must not delete META-INF/services from a jar. Otherwise the jar will fail to register as a runtime extension. 
### This led to the error "SAXNotRecognizedException: Feature : http://apache.org/xml/features/validation/schema"
###
###
### Currently (May 2005) the following jars need this extra treatment:
###   cosyframework-core.jar
###   cosyframeowrk-gui.jar
###   cosyicons.jar
###   skinlf.jar
###   xercesImpl.jar
###   xmlParserAPIs.jar
###


if [ ! $1 ] ; then
   echo "Go to the directory where the jar is, then use this script from there"
   echo "Usage: (this) jarfile"
   exit 2
fi

location_of_this_script=$PWD/`dirname $0`
t=tmpdir
j=$1
s="jarsigner -keystore $location_of_this_script/EsoKeystore -storepass 2Garch1ng $j eso"

echo preparing...
rm -rf $t 2>/dev/null
mkdir -p $t

echo extracting jar...
mv $j $t
cd $t
jar xf $j
mv $j ..

echo rebuilding jar...
rm -f META-INF/Manifest.mf
rm -f META-INF/MANIFEST.MF
jar cf ../$j *
cd ..


echo signing jar...
$s $j

echo cleaning up...
rm -rf $t 2>/dev/null

echo done.

