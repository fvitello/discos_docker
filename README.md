
This work is adapted from https://github.com/ACS-Community/ACS-Docker-Image

## Build Container

docker build -t fvitello/discos  .

## Run Container

docker run --rm -it -e DISPLAY=host.docker.internal:0 --mount type=volume,source=discos-sw,destination=/discos-sw fvitello/discos


## Note for x11 on mac

https://gist.github.com/sorny/969fe55d85c9b0035b0109a31cbcb088