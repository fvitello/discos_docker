FROM centos:centos8.1.1911

#General packages
RUN dnf -y install vim redhat-lsb-core sudo dbus-x11 rsync dnf-plugins-core
RUN dnf config-manager --set-enabled PowerTools

#Install Git and Git LFS
RUN dnf -y install epel-release
RUN dnf -y install https://download1.rpmfusion.org/free/el/rpmfusion-free-release-8.noarch.rpm
RUN dnf -y install git
RUN curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.rpm.sh | bash
RUN dnf -y install git-lfs

#Install Java 11
RUN dnf -y install java-11-openjdk-devel
#RUN echo 1| alternatives --config java_sdk_openjdk

#Prepare for Java
RUN mkdir -p /usr/java
RUN ln -s /usr/lib/jvm/java-openjdk /usr/java/default

#Install ACS required packages
RUN dnf -y install ksh gcc gcc-c++ gcc-gfortran libX11-devel libffi-devel perl readline-devel bzip2 bzip2-devel openssl-devel openldap-devel libxml2-devel freetype-devel libxslt-devel sqlite-devel expat-devel tcl-devel tk-devel bison flex autoconf unzip dos2unix

#RTI DDS
#RUN cd / && curl http://*****:*****@webdav.sco.alma.cl/restricted/propietary/RTI-RHEL6-x86_64.tar.bz2 | tar -xj

RUN dnf -y install gdb net-tools

# Prepare it to be a jenkins node
RUN sed -ri 's/UsePAM yes/#UsePAM yes/g' /etc/ssh/sshd_config && \
    sed -ri 's/#UsePAM no/UsePAM no/g' /etc/ssh/sshd_config && \
    adduser jenkins && echo new2me | passwd jenkins --stdin && \
    chown jenkins.jenkins -R /alma && \
    cd /home/jenkins && wget http://webdav.sco.alma.cl/public/tat_parser.py &&\
    chmod a+x tat_parser.py
