FROM centos:centos7.9.2009
# ================ Builder stage =============================================
# we base our image on a vanilla Centos 7 image.
ENV ACS_PREFIX=/alma ACS_VERSION="2021.12"
ENV ACS_ROOT=$ACS_PREFIX/acs
ENV JAVA_HOME="/usr/java/default"

# install deltarpm prior to installing everything else
# it might save some time during downloading and installing the
# dependencies below, but it is not urgently needed for ACS to work
# c.f. https://www.cyberciti.biz/faq/delta-rpms-disabled-because-applydeltarpm-not-installed/
RUN yum update -y && yum install -y deltarpm && \
    yum -y install epel-release && \
    yum -y groupinstall "Development Tools" && \
    yum -y install  autoconf \
                    bison \
                    bzip2 \
                    bzip2-devel \
                    dos2unix \
                    epel-release \
                    expat-devel \
                    file \
                    flex \
                    freetype-devel \
                    gcc \
                    gcc-c++ \
                    gcc-gfortran \
                    git \
                    java-11-openjdk \
                    java-11-openjdk-devel \
                    lbzip2 \
                    lbzip2-utils \
                    libffi \
                    libffi-devel \
                    libX11-devel \
                    libxml2-devel \
                    libxslt-devel \
                    lockfile-progs \
                    make \
                    net-tools \
                    openldap-devel \
                    openssh-server \
                    openssl-devel \
                    perl \
                    procmail \
                    python-devel \
                    python2-pip \
                    python3-pip \
                    readline-devel \
                    redhat-lsb-core \
                    rpm-build \
                    sqlite-devel \
                    tcl-devel \
                    tk-devel \
                    xauth \
                    curl \
                    sudo \
                    git-lfs \
                    ksh \
                    mc \
                    nc \
                    patch \
                    screen \
	                subversion \
                    unzip \
                    vim \
	                wget \
                    tree \
                    xterm && \
    yum clean all && \
    # Prepare Java
    mkdir -pv /usr/java && \
    ln -sv /usr/lib/jvm/java-openjdk $JAVA_HOME 
	
RUN cd / && git clone  --recursive https://bitbucket.alma.cl/scm/asw/acs.git && \
    cd /acs && git checkout acs/2021DEC && git submodule update --init

## Get missing (super old) libraries
RUN cd /acs/ExtProd/PRODUCTS && \
    wget https://sourceforge.net/projects/gnuplot-py/files/Gnuplot-py/1.8/gnuplot-py-1.8.tar.gz/download -O gnuplot-py-1.8.tar.gz && \
    wget https://sourceforge.net/projects/pychecker/files/pychecker/0.8.17/pychecker-0.8.17.tar.gz/download -O pychecker-0.8.17.tar.gz && \
    wget https://sourceforge.net/projects/numpy/files/OldFiles/1.3.3/numarray-1.3.3.tar.gz && \
    # some versions for python dependencies have changed.
    # Also we removed the *bulkDataNT* and *bulkData* modules from the Makefile
    # as we don't have the properietary version of DDS and don't use this modules.
    # sed -i '185 a execute_build_script "buildDDS" ./buildDDS &&  echo_success || echo_failure' /acs/ExtProd/INSTALL/buildTools  && \
    cd /acs/ExtProd/INSTALL && \
    source /acs/LGPL/acsBUILD/config/.acs/.bash_profile.acs && \
#patch cppunit and doxygen
    sed -i 's/.\/configure/.\/configure --build=aarch64-unknown-linux-gnu /g' /acs/ExtProd/PRODUCTS/acs-ext/cppunit/src/Makefile && \
    sed -i 's/.\/configure/.\/configure --build=aarch64-unknown-linux-gnu /g' /acs/ExtProd/PRODUCTS/acs-ext/doxygen/src/Makefile && \
    time make all


    time make all && \
# build opendds
RUN cd /acs/ExtProd/INSTALL && \
    source /acs/LGPL/acsBUILD/config/.acs/.bash_profile.acs  && \
    sed -i 's/xerces3       = 0/xerces3       = 1/g' /alma/ACS-2021DEC/TAO/ACE_wrappers/bin/MakeProjectCreator/config/global.features && \
    sed -i '58 a pushd $ACE_ROOT/ace' /acs/ExtProd/INSTALL/buildDDS && \
    sed -i '59 a XERCESCROOT=$ALMASW_INSTDIR\/ExternalDependencies make -j $NUM_CPU' /acs/ExtProd/INSTALL/buildDDS && \
    sed -i '60 a popd' /acs/ExtProd/INSTALL/buildDDS && \
    sed -i 's/rm -rf $ALMASW_INSTDIR\/DDS//g' /acs/ExtProd/INSTALL/buildDDS && \
    sed -i '62 a cd $DDS_ROOT/dds/DCPS/QOS_XML_Handler' /acs/ExtProd/INSTALL/buildDDS && \
    ./buildDDS && \
        find /alma -name "*.o" -exec rm -v {} \;


# --------------------- Here external dependencies are built, then delete build folder --------------
RUN cd /acs && \
    source /acs/LGPL/acsBUILD/config/.acs/.bash_profile.acs && \
    sed -i 's/bulkDataNT/bulkDataNTOpenDDS/g' /acs/Makefile && \
    time make build && \
    rm -rf /acs

WORKDIR /
# Here we create and configure the user almamgr
RUN groupadd -g 1000 almamgr && \
    useradd -g 1000 -u 1000 -d /home/almamgr -m -s /bin/bash almamgr && \
    passwd -d almamgr   && \
    groupadd sudo  && \
    usermod -aG sudo almamgr  && \
    echo "%sudo	ALL=(ALL)	ALL" >> /etc/sudoers  && \
    echo "%sudo	ALL=(ALL)	NOPASSWD: ALL" >> /etc/sudoers  && \
    chown -R almamgr:almamgr /alma
# For conveniece we source the alma .bash_profile.acs in the user .bash_rc
# and export JAVA_HOME
RUN    echo "source /alma/ACS-2021DEC/ACSSW/config/.acs/.bash_profile.acs" >> /home/almamgr/.bashrc && \
       echo "export JAVA_HOME=$JAVA_HOME" >> /home/almamgr/.bashrc

USER almamgr