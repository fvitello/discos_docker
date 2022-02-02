#!/usr/bin/env python

EXCLUDE = ['all', 'clean', 'clean_all', 'clean_dist_all', 'do_all', 'install', 'install_all']
def fix_all(matchobj):
    extras = ' '.join(filter(lambda x: x not in EXCLUDE, matchobj.groups()[0].split()))
    return '$(MODRULE)all: $(MODPATH) $(MODDEP) {}'.format(extras).rstrip()

def fix_clean(matchobj):
    extras = ' '.join(filter(lambda x: x not in EXCLUDE, matchobj.groups()[0].split()))
    return '$(MODRULE)clean: $(MODPATH) clean_$(MODDEP) {}'.format(extras).rstrip()

def fix_clean_dist(matchobj):
    extras = ' '.join(filter(lambda x: x not in EXCLUDE, matchobj.groups()[0].split()))
    return '$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP) {}'.format(extras).rstrip()

def fix_install(matchobj):
    extras = ' '.join(filter(lambda x: x not in EXCLUDE, matchobj.groups()[0].split()))
    return '$(MODRULE)install: $(MODPATH) install_$(MODDEP) {}'.format(extras).rstrip()

ALL_RE = r'^all\s*:(.*)$'
CLEAN_RE = r'^clean\s*:(.*)$'
CLEAN_DIST_RE = r'^clean_dist\s*:(.*)$'
INSTALL_RE = r'^install\s*:(.*)$'
ECHO_RE = r'\t@echo " . . . .* done"'
def fix_module_mk(contents):
    import re
    # Fix up the default rules ('all', 'clean', 'clean_dist', 'install')
    contents = re.sub(ALL_RE, fix_all, contents, flags=re.M)
    contents = re.sub(CLEAN_RE, fix_clean, contents, flags=re.M)
    contents = re.sub(CLEAN_DIST_RE, fix_clean_dist, contents, flags=re.M)
    contents = re.sub(INSTALL_RE, fix_install, contents, flags=re.M)
    # Delete default 'man' and 'db' rules; this only negatively affects 1 file in all of
    # almasw that I could find (most are empty)
    contents = re.sub(r'^man\s*:.*$\n(^\t.*$\n)*', '', contents, flags=re.M)
    contents = re.sub(r'^db\s*:.*$\n(^\t.*$\n)*', '', contents, flags=re.M)
    # Delete include of old acsMakefile
    contents = re.sub(r'^MAKEDIRTMP := \$\(shell searchFile include/acsMakefile\).*^endif$',
                      '', contents, flags=re.M|re.S)
    contents = re.sub(r'^ifdef ACSROOT$.*^endif$', '', contents, flags=re.M|re.S)
    # Replace default echo rules with the latest format
    contents = re.sub(ECHO_RE, '\t$(AT)echo " . . . $@ done"', contents)
    return contents

if __name__ == '__main__':
    import sys
    if len(sys.argv) != 2:
        print('Usage: {} <filename>'.format(sys.argv[0]))
        exit(0)
    with open(sys.argv[1]) as f:
        contents = f.read()
        print(fix_module_mk(contents))
