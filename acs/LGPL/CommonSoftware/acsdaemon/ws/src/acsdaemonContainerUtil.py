#!/usr/bin/env python

import sys
import argparse
from ACSContainerDaemonUtils import *

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Utility functions for ACS Container Daemon')
    parser.add_argument('action', choices=['stack', 'core'], help="Set the action to run for this command")
    parser.add_argument('lang', choices=['cpp', 'java', 'py'], help="Choice the container language runtime")
    parser.add_argument('container_name', help="Container on which the action will be executed")
    args = parser.parse_args()

    ret_val = None
    out = None
    if args.action == 'stack':
        if args.lang == 'cpp':
            out = get_c_stacktrace(find_container_pid(args.container_name))
        elif args.lang == 'java':
            out = get_java_stacktrace(find_container_pid(args.container_name))
        elif args.lang == 'py':
            out = get_py_stacktrace(find_container_pid(args.container_name))
    elif args.action == 'core':
        out = generate_core_dump(find_container_pid(args.container_name))

    print(out)
    sys.exit(0)
