#!/usr/bin/env python

import subprocess
import psutil

'''
Contains helper functions to get info from ACS containers
'''


def __find_myself_as_args(args):
    for a in args:
        if a == __file__:
            return True
    return False


def find_container_pid(container_name):
    for proc in psutil.process_iter():
        for arg in proc.as_dict()['cmdline']:
            if arg == container_name:
                if proc.name() == 'maciContainer':
                    return proc.pid
                elif proc.name() == 'java':
                    return proc.pid
                elif (proc.name() == 'python' or proc.name() == 'python2' or proc.name() == 'python3') \
                        and not __find_myself_as_args(proc.as_dict()['cmdline']):
                    return proc.pid
    raise Exception("Container not found")


def generate_core_dump(pid):
    process = subprocess.Popen(['gcore', str(pid)], stdout=subprocess.PIPE)
    stdoutdata = process.communicate(None)[0]
    return stdoutdata


def get_java_stacktrace(pid):
    process = subprocess.Popen(['jstack', '-l', str(pid)], stdout=subprocess.PIPE)
    stdoutdata = process.communicate(None)[0]
    return stdoutdata


def get_c_stacktrace(pid):
    process = subprocess.Popen(['gdb', '-p', str(pid), '-ex', 'thread apply all bt', '-ex', 'detach', '-ex', 'quit'],
                               stdout=subprocess.PIPE)
    stdoutdata = process.communicate(None)[0]
    return stdoutdata


def get_py_stacktrace(pid):
    process = subprocess.Popen(['gdb', '-p', str(pid), '-ex', 'thread apply all py-bt', '-ex', 'detach', '-ex', 'quit'],
                               stdout=subprocess.PIPE)
    stdoutdata = process.communicate(None)[0]
    return stdoutdata


