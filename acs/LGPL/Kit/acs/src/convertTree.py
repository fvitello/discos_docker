#!/usr/bin/env python

import os
from acsKit.groupMk import fix_group_mk
from acsKit.groupMakefileMk import groupMakefileMk
from acsKit.moduleMk import fix_module_mk
from acsKit.moduleMakefileMk import moduleMakefileMk

def is_module(root):
    return root.endswith('src') or root.endswith('test')

def get_group_paths(start):
    paths = []
    for root, dirs, files in os.walk(start):
        if not is_module(root) and 'Makefile' in files:
            paths.append(root)
    return paths

def get_module_paths(start):
    paths = []
    for root, dirs, files in os.walk(start):
        if is_module(root) and 'Makefile' in files:
            paths.append(root)
    return paths

def convert_groupMk(paths):
    print('Paths for group Makefiles (convert by hand for now):')
    for path in paths:
        print(path)
        #contents = ''
        #with open(os.path.join(path, 'Makefile'), 'r') as f:
        #    contents = fix_group_mk(f.read())
        #with open(os.path.join(path, 'group.mk'), 'w+') as f:
        #    f.write(contents)

def convert_moduleMk(paths):
    for path in paths:
        contents = ''
        with open(os.path.join(path, 'Makefile'), 'r') as f:
            contents = fix_module_mk(f.read())
        with open(os.path.join(path, 'module.mk'), 'w+') as f:
            f.write(contents)

def write_groupMakefileMk(paths):
    contents = groupMakefileMk()
    for path in paths:
        with open(os.path.join(path, 'Makefile.mk'), 'w+') as f:
            f.write(contents)

def write_moduleMakefileMk(paths):
    contents = moduleMakefileMk()
    for path in paths:
        with open(os.path.join(path, 'Makefile.mk'), 'w+') as f:
            f.write(contents)

if __name__ == '__main__':
    import sys
    if len(sys.argv) != 2:
        print('Usage: {} <path>'.format(sys.argv[0]))
        exit(0)
    module_paths = get_module_paths(sys.argv[1])
    convert_moduleMk(module_paths)
    write_moduleMakefileMk(module_paths)

    group_paths = get_group_paths(sys.argv[1])
    convert_groupMk(group_paths)
    write_groupMakefileMk(group_paths)
