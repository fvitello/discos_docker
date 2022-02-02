### cd /acs_build/BUILD-5.0/ACS/Web/Home/Releases/DevelopmentHead/Distribution
### cd Linux
### scp ACS-RH9.tar.gz ACSsources.tar.gz ACS.tar.gz gtar gunzip gzip INSTALL INSTALL.tar.gz README almamgr@te22.hq.eso.org:CVSWORKS/ACS/Web/Home/Releases/ACS_5_0/Distribution/Linux
### scp -r INSTALL almamgr@te22.hq.eso.org:CVSWORKS/ACS/Web/Home/Releases/ACS_5_0/Distribution/Linux
### cd ../Linux-NO-LGPL
### scp ACS-NO-LGPL-RH9.tar.gz ACS-NO-LGPL.tar.gz ACSsources.tar.gz almamgr@te22.hq.eso.org:CVSWORKS/ACS/Web/Home/Releases/ACS_5_0/Distribution/Linux-NO-LGPL
### cd ../Linux-NO-LGPL-VX
### scp ACSSW-vw.tar.gz ACS-vw.tar.gz INSTALL INSTALL.tar.gz vw5.5.tar.gz almamgr@te22.hq.eso.org:CVSWORKS/ACS/Web/Home/Releases/ACS_5_0/Distribution/Linux-NO-LGPL-VX
### scp -r INSTALL almamgr@te22.hq.eso.org:CVSWORKS/ACS/Web/Home/Releases/ACS_5_0/Distribution/Linux-NO-LGPL-VX

rsync -av --progress --exclude=CVS --exclude='*~' /acs_build/BUILD-5.0/ACS/Web/Home/Releases/DevelopmentHead/Distribution/ almamgr@te22.hq.eso.org:CVSWORKS/ACS/Web/Home/Releases/ACS_5_0/Distribution
ssh almamgr@te22.hq.eso.org chmod -R oug+r CVSWORKS/ACS/Web/Home/Releases/ACS_5_0/Distribution
