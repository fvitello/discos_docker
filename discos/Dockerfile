FROM fvitello/acs202112
USER root
VOLUME  /discos-sw

# ansible-> roles/user/task
RUN mkdir /discos-sw  && \
    chmod 755 /discos-sw && \
    #Add the users
    groupadd -g 1002 projects && \
    useradd -g 1000 -G 1002 -u 3060 -d /discos-sw/discos -m -s /bin/bash discos && \
    passwd -d discos && \
    #Tune the /discos-sw directory ownership
    chown -R discos:almamgr /discos-sw && \
    #Give administrator permissions
    usermod -aG sudo discos && \
    #Create the DISCOS archive directory
    mkdir /archive && \
    chmod 755 /archive  && \
    chown -R discos:projects /archive

# ansible-> roles/acs/task/discos_environment
#Populate the {{ discos_sw_dir }} directory tree
RUN mkdir /discos-sw/config  && \
    chmod 750 /discos-sw/config && \ 
    mkdir /discos-sw/config/misc  && \
    chmod 750 /discos-sw/config/misc && \ 
    mkdir /discos-sw/introots  && \
    chmod 711 /discos-sw/introots && \ 
    mkdir /discos-sw/config/acs  && \
    mkdir -p /service/acstmp  && \
    chown discos:almamgr /discos-sw/config && \
    chown discos:almamgr /discos-sw/config/misc && \
    chown discos:almamgr /discos-sw/introots && \
    chown discos:almamgr /discos-sw/config/acs && \
    chown discos:almamgr /service/acstmp

#Copy the bashrc template
COPY --chown=discos:almamgr --chmod=640 templates/acs/bashrc /discos-sw/config/misc/bashrc
#Copy the bash_profile template
COPY --chown=discos:almamgr --chmod=640 templates/acs/bash_profile /discos-sw/config/misc/bash_profile
#Copy the prompt_command.sh template
# NOTE FIX THE CONTENT 
COPY --chown=discos:almamgr --chmod=640 templates/acs/prompt_command.sh /discos-sw/config/misc/prompt_command.sh
#Copy the load_branch template
COPY --chown=discos:almamgr --chmod=640 templates/acs/load_branch /discos-sw/config/misc/load_branch
#Create the  the branches and station file,  and the /bin directory for user discos
RUN touch /discos-sw/config/misc/branches && \ 
    chmod 640 /discos-sw/config/misc/branches && \ 
    chown discos:almamgr /discos-sw/config/misc/branches && \
    printf "#!/usr/bin/env bash\n\nexport STATION=development\n" >> /discos-sw/config/misc/station && \
    chmod 640 discos-sw/config/misc/station && \ 
    chown discos:almamgr discos-sw/config/misc/station  && \
    mkdir -p /discos-sw/discos/bin  && \
    chmod 700 /discos-sw/discos/bin && \ 
    chown discos:almamgr discos-sw/discos/bin
#Render the discos-get template
COPY --chown=discos:almamgr --chmod=500 templates/acs/discos-get /discos-sw/discos/bin/discos-get
#Render the discos-set template
COPY --chown=discos:almamgr --chmod=500 templates/acs/discos-set /discos-sw/discos/bin/discos-set
#Render the _discos-check-branch template
COPY --chown=discos:almamgr --chmod=500 templates/acs/_discos-check-branch /discos-sw/discos/bin/_discos-check-branch
#Copy the pyrc template
COPY --chown=discos:almamgr --chmod=600 templates/acs/pyrc /discos-sw/discos/.pyrc
#Copy the vimrc template
COPY --chown=discos:almamgr --chmod=600 templates/acs/vimrc /discos-sw/discos/.vimrc
#Add the custom bashrc sourcing to the default one
RUN printf "\n######## DISCOS configuration BEGIN ########\nif [ -f /discos-sw/config/misc/bashrc ]; then\n\tsource /discos-sw/config/misc/bashrc\nfi\n######## DISCOS configuration END ########\n" >>  /discos-sw/discos/.bashrc && \
    printf "\n######## DISCOS configuration BEGIN ########\nif [ -f /discos-sw/config/misc/bash_profile ]; then\n\tsource /discos-sw/config/misc/bash_profile\nfi\n######## DISCOS configuration END ########\n" >>  /discos-sw/discos/.bash_profile

USER discos

# ansible-> roles/acs/task/acs
# Copy the .acs dir from /alma to /discos-sw/config
RUN cp -r /alma/ACS-2021DEC/ACSSW/config/.acs/. /discos-sw/config/acs && \
    #Create the acstmp directory    
    mkdir -p /service/acstmp/discos-manager
#Copy the acsStartContainer with debugger
COPY --chown=discos:almamgr --chmod=755 templates/acs/acsStartContainerGdb /alma/ACS-2021DEC/ACSSW/bin/acsStartContainerGdb
#Remove the ACS boost directory
# RUN rm -rf /alma/ACS-2021DEC/boost/

# ansible-> roles/acs/task/discos_dependencies
RUN mkdir -p  /tmp/dependencies
USER root
#cfitsio ccfits fv modbus f2c

COPY  templates/dependencies/cfitsio3370.tar.gz /tmp/dependencies/cfitsio3370.tar.gz
COPY  templates/dependencies/CCfits-2.4.tar.gz /tmp/dependencies/CCfits-2.4.tar.gz
COPY  templates/dependencies/fv5.4_pc_linux64.tar.gz /tmp/dependencies/fv5.4_pc_linux64.tar.gz
COPY templates/dependencies/f2c.zip /tmp/dependencies/f2c.zip
COPY templates/dependencies/libmodbus-3.0.6.tar.gz /tmp/dependencies/libmodbus-3.0.6.tar.gz

RUN cd /tmp/dependencies/ && \
    tar xvzf cfitsio3370.tar.gz && \
    cd /tmp/dependencies/cfitsio && \
    ./configure --prefix=/usr/local && \
    make && \
    make install && \
    cd /tmp/dependencies/ && \
    tar xvzf CCfits-2.4.tar.gz && \
    cd /tmp/dependencies/CCfits && \
    ./configure --prefix=/usr/local && \
    make && \
    make install && \
    cd /tmp/dependencies/ && \
    tar xvzf fv5.4_pc_linux64.tar.gz && \
    cp -r fv5.4 /usr/local/lib && \
    echo '/usr/local/lib/fv5.4/fv' | tee -a /usr/local/bin/fv > /dev/null && \
    chmod 755 /usr/local/bin/fv && \
    cd /tmp/dependencies/ && \
    tar xvzf libmodbus-3.0.6.tar.gz && \
    cd /tmp/dependencies/libmodbus-3.0.6 && \
    ./configure --prefix=/usr/local && \
    make && \
    make install  && \
    cd /tmp/dependencies/ && \
    unzip f2c.zip && \
    cd f2c-master/src && \
    cp makefile.u Makefile&& \
    make && \
    cp f2c.h /usr/local/include && \
    cp f2c /usr/local/bin && \
    rm -rf /tmp/dependencies/

#######################
# QT AND PYQT LIBRARIES
#######################
COPY templates/dependencies/qt-x11-opensource-src-4.5.2.tar.gz_00 /tmp/dependencies/qt-x11-opensource-src-4.5.2.tar.gz_00
COPY templates/dependencies/qt-x11-opensource-src-4.5.2.tar.gz_01 /tmp/dependencies/qt-x11-opensource-src-4.5.2.tar.gz_01

# Merge qt parts and No license confirmation prompt during installation
#Increment the process stack size in order to compile AS GUI
RUN ulimit -s 65532 && \
    cat /tmp/dependencies/qt-x11-opensource-src-4.5.2.tar.gz_* >> /tmp/dependencies/qt-x11-opensource-src-4.5.2.tar.gz && \
    cd /tmp/dependencies/ && \
    tar xvzf /tmp/dependencies/qt-x11-opensource-src-4.5.2.tar.gz && \
    cd /tmp/dependencies/qt-x11-opensource-src-4.5.2 && \
    sed -i 's/OPT_CONFIRM_LICENSE=no/OPT_CONFIRM_LICENSE=yes/g' /tmp/dependencies/qt-x11-opensource-src-4.5.2/configure && \
    #Install Qt 
    yum install -y libX11-devel libXrender-devel libXext-devel libpng12-devel && \
    rm /usr/lib64/libpng.so && \
    ln -s /usr/lib64/libpng12.so /usr/lib64/libpng.so && \
    cd /tmp/dependencies/qt-x11-opensource-src-4.5.2 && \
    ./configure -no-openssl -opensource --prefix=/usr/local/qt-4.5.2 && \
    #PATCH to use libpng12 and fpermissive
    sed -i 's/libpng15/libpng12/g' /tmp/dependencies/qt-x11-opensource-src-4.5.2/src/gui/Makefile && \
    sed -i 's/CXXFLAGS      =/CXXFLAGS      = -fpermissive/g' /tmp/dependencies/qt-x11-opensource-src-4.5.2/src/xmlpatterns/Makefile && \
    sed -i 's/CXXFLAGS      =/CXXFLAGS      =-fpermissive/g' /tmp/dependencies/qt-x11-opensource-src-4.5.2/src/3rdparty/webkit/WebCore/Makefile && \
    sed -i 's/CXXFLAGS      =/CXXFLAGS      =-fpermissive/g' /tmp/dependencies/qt-x11-opensource-src-4.5.2/tools/porting/src/Makefile

RUN cd /tmp/dependencies/qt-x11-opensource-src-4.5.2 && \
    gmake &> compile.log && \
    gmake install && \
    rm -rf /tmp/dependencies/*

RUN chmod 777 /tmp/dependencies/


#sip
COPY  templates/dependencies/sip-4.19.2.tar.gz /tmp/dependencies/sip-4.19.2.tar.gz
COPY  templates/dependencies/PyQt4_gpl_x11-4.12.tar.gz /tmp/dependencies/PyQt4_gpl_x11-4.12.tar.gz
COPY  templates/dependencies/PyQwt-5.2.0.tar.gz /tmp/dependencies/PyQwt-5.2.0.tar.gz

SHELL ["/bin/bash", "-c"]

RUN source /discos-sw/config/acs/.bash_profile.acs && \
    cd /tmp/dependencies/ && \
    tar xvzf sip-4.19.2.tar.gz && \
    cd /tmp/dependencies/sip-4.19.2 && \
    python2 configure.py && \
    make && \
    make install  && \
    cd /tmp/dependencies/ && \
    tar xvzf PyQt4_gpl_x11-4.12.tar.gz && \
    cd /tmp/dependencies/PyQt4_gpl_x11-4.12 && \
    python2 configure.py --confirm-license --qmake /usr/local/qt-4.5.2/bin/qmake  && \
    sed -i "s/^.*PrintCurrentPage.*$//g" /tmp/dependencies/PyQt4_gpl_x11-4.12/QtGui/sipAPIQtGui.h  && \
    sed -i "s/^.*PrintCurrentPage.*$//g" /tmp/dependencies/PyQt4_gpl_x11-4.12/QtGui/sipQtGuiQAbstractPrintDialog.cpp && \
    sed -i "s/^.*PrintCurrentPage.*$//g" /tmp/dependencies/PyQt4_gpl_x11-4.12/sip/QtGui/qabstractprintdialog.sip && \
    cd /tmp/dependencies/PyQt4_gpl_x11-4.12 && \
    make && \
    make install 


RUN cd /tmp/dependencies/ && \
    git clone https://github.com/PyQwt/PyQwt5.git 

COPY  templates/dependencies/qwt-5.2.zip /tmp/dependencies/PyQwt5/qwt-5.2.zip

RUN source /discos-sw/config/acs/.bash_profile.acs && \
    cd /tmp/dependencies/ && \
    tar xvzf PyQwt-5.2.0.tar.gz && \
    cd /tmp/dependencies/PyQwt-5.2.0/configure && \
    python2 configure.py -Q ../qwt-5.2 && \
    make && \
    make install && \
    #Install some third-party Python packages
    pip2 install wheel && \
    pip2 install pbr mock==1.1.1 jinja2 subprocess32 d2to1 stsci.distutils pyfits  prospector==0.12.11 && \
    pip2 cache purge && \
    rm -rf /tmp/dependencies/*

COPY --chown=discos:almamgr --chmod=755  templates/dependencies/SlaLibrary.tar.gz /tmp/dependencies/SlaLibrary.tar.gz
RUN mkdir /discos-sw/SlaLibrary/ && \
    tar xvzf /tmp/dependencies/SlaLibrary.tar.gz --directory /discos-sw/SlaLibrary/

USER discos
RUN source /discos-sw/config/acs/.bash_profile.acs && \
    ipython2 profile create

COPY --chown=discos:almamgr --chmod=644  templates/acs/ipython_startup_01.py /discos-sw/discos/.ipython/profile_default/startup/01.py
# ansible-> roles/acs/task/receivers_monitor
USER root
#Create the /service/receivers directory
RUN mkdir /service/Receivers && \
    chmod 755 /service/Receivers && \ 
    chown discos:almamgr /service/Receivers && \
    printf "\n######## Receivers monitoring ########\nif [ -f /discos-sw/introots/default/bin/receiversmonitor.py ]; then\n\trunuser -l discos -c 'python2 receiversmonitor.py &'\nfi\n" >>  /etc/rc.local && \
    printf "#discos-logrotate\n59 23 * * * /usr/sbin/logrotate -f /discos-sw/config/misc/discos-logrotate" >>  /var/spool/cron/discos && \
    mkdir /service/events && \
    chmod 755 /service/events && \
    chown discos:almamgr /service/events

USER discos
WORKDIR /discos-sw/discos    

RUN mkdir /archive/data && \
    mkdir /archive/extraData && \
    mkdir /archive/events && \
    mkdir /archive/logs && \
    mkdir /archive/schedules && \
    chmod 710 /archive/data && \
    chmod 710 /archive/extraData && \
    chmod 750 /archive/events && \
    chmod 750 /archive/logs && \
    chmod 710 /archive/schedules 

COPY --chown=discos:almamgr --chmod=644  templates/manager/discos-logrotate /discos-sw/config/misc/discos-logrotate


