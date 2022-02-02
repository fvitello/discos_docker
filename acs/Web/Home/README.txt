G.Chiozzzi - 2003-12-01
--------------------------
CVS ARCHIVE

G.Chiozzi - 2004-01-20
I have started putting the web pagde in CVS one piece at the time.
The page is in:

             ACS/Web/Home

in the ACS Module.
Step by step all files needed to reconstruct the page will be
archived.
See notes here at the end.

WARNING: Files come out of CVS without world read permission.
         Therefore permisisons have to be changed before publishing on
         the web.
---------------

BUILDING THE PAGE

A number of sub-pages shall be generated.
We are working to write Makefiles to to all of this from a basic make
all command, but
the problem is that some commands run on Linux, some on Windows and
some can be run only manually, like generating PDF from Word documents.

--------------
LOCATION OF THE PAGE

The ACS web page is currently physically on the ESO filer in:

     ~almamgr/Acs/acsdoc

The official URL for the ACS web page on the ESO web is: 
   
     /home/web/eso/docs/projects/alma/develop/acs

This redirects automatically and transparently to the physical location.

The official URL is located on web4
If and when we will have the pages physically there,, to put them 
online the webcp command shall be used:

        /usr/server/bin/webcp index.html

This command should be in the path.

-----------------

TODO and status of files

The following directories will not be archived:

- tmp
