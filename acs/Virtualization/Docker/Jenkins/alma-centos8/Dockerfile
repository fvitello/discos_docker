FROM acs/centos8

#ARCHIVE
RUN dnf -y install libtool libdb-devel

#ICD
RUN dnf -y install dnf-plugins-core
RUN dnf config-manager --set-enabled PowerTools
RUN dnf -y install cmake blas-devel cfitsio-devel wcstools-devel lapack-devel

#CONTROL
RUN dnf -y install binutils-devel libmemcached-devel

#CORRCommon
RUN dnf -y install kernel kernel-devel procmail

#TELCAL
RUN dnf -y install motif-devel texlive
