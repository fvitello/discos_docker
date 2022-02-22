
# ACS 2021DEC

ACS container is is adapted from https://github.com/ACS-Community/ACS-Docker-Image


## Build Container

`docker build -t fvitello/acs202112  .`

# Discos

## Prerequisites

To build the discos container please add in `discos/templates/dependencies` the following prerequisites:
- SlaLibrary.tar.gz
- sip-4.19.2.tar.gz
- qwt-5.2.zip
- qt-x11-opensource-src-4.5.2.tar.gz_00
- qt-x11-opensource-src-4.5.2.tar.gz_01
- PyQwt-5.2.0.tar.gz
- PyQt4_gpl_x11-4.12.tar.gz
- libmodbus-3.0.6.tar.gz
- fv5.4_pc_linux64.tar.gz
- f2c.zip
- cfitsio3370.tar.gz
- CCfits-2.4.tar.gz

## Build Container

`docker build -t fvitello/discos  .`

## Run Container

`docker run --rm -it -e DISPLAY=host.docker.internal:0 --mount type=volume,source=discos-sw,destination=/discos-sw fvitello/discos`

# Note for x11 on mac

https://gist.github.com/sorny/969fe55d85c9b0035b0109a31cbcb088
