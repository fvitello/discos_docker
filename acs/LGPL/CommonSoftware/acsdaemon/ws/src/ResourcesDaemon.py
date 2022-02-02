#!/usr/bin/env python

import os
import sys
import time
import json
import signal
import traceback

#In the future use psutil!
#import psutil
import shlex
import subprocess

class ExitException(Exception):
    pass

class Task:
    def __init__(self):
        self.res = None
        self.deps = []
        self.json = {}
    def printContents(self, ind=''):
        print(ind + self.json["type"] + ": " + self.json["target"])
    def addDep(self, dep):
        self.deps.append(dep)
    def setContent(self, json):
        self.json = json
    def setResource(self, res):
        self.res = res
    def execute(self):
        raise NotImplementedError()

class KillTask(Task):
    def execute(self):
        out = None
        rc = None
        err = None
        if self.json["target"] == "self":
            pid = Resource.getPID(self.res)
            print("Killing local process " + str(pid))
            [out, rc, err] = Resource.system_call("kill -9 " + str(pid))
        else:
            for r in self.deps:
                if r.json["name"] == self.json["target"]:
                    if r.json["machine"] == os.getenv("HOST"):
                        pid = Resource.getPID(r)
                        print("Killing local process " + str(pid))
                        [out, rc, err] = Resource.system_call("kill -9 " + str(pid))
                    else:
                        pid = Resource.getRemotePID(r)
                        print("Killing remote process " + str(pid))
                        [out, rc, err] = Resource.system_call("ssh " + os.getenv("HOST") + " kill -9 " + str(pid))
                    break
        if out:
            print(out)
        if rc:
            print(rc)
        if err:
            print(err)

class EmailTask(Task):
    def execute(self):
        print("Sending e-mail: " + self.json["target"] + ": " + self.json["message"])

class ResourceLimit:
    @staticmethod
    def delegateTasks(rls):
        MemoryLimit.delegateTasks(rls)
    def __init__(self):
        self.res = None
        self.tasks = []
        self.deps = []
        self.json = {}
    def printContents(self, ind=''):
        print(ind + self.json["type"] +": " + str(self.json["limit"]))
        for t in self.tasks:
            t.printContents(ind + '   ')
    def addDep(self, dep):
        self.deps.append(dep)
        for t in self.tasks:
            t.addDep(dep)
    def setContent(self, json):
        self.json = json
    def setResource(self, res):
        self.res = res
    def addTask(self, t):
        self.tasks.append(t)
    def checkResource(self):
        raise NotImplementedError()
    def executeTasks(self):
        for t in self.tasks:
            t.execute()

class MemoryLimit(ResourceLimit):
    @staticmethod
    def delegateTasks(rls):
        rle = None
        for rl in rls:
            if isinstance(rl, MemoryLimit) and (rle == None or rl.json["limit"] > rle.json["limit"]):
                rle = rl
        if rle:
            rle.executeTasks()
    def checkResource(self):
        self.conv = {"kB": 1024, "mB": 1048576, "gB": 1073741824}
        pid = Resource.getPID(self.res)
        if pid:
            f = open("/proc/" + str(pid) + "/status")
            status = f.readlines()
            f.close()
            rss = ""
            for s in status:
                if "VmRSS" in s:
                    rss = s
            rsse = rss.strip().split(' ')
            mem = int(rsse[-2])*self.conv[rsse[-1]]
            if mem > self.json["limit"]:
                print("Memory limit " + str(self.json["limit"]) + " was exceeded (" + str(mem) + "), scheduling tasks.")
                return True
        return False

class Resource:
    @staticmethod
    def getPID(rs):
        dirs = os.listdir("/proc/")
        for d in dirs:
            if os.path.isfile("/proc/" + d + "/cmdline"):
                f = open("/proc/" + d + "/cmdline")
                cmd = f.read()
                f.close()
                if rs.json["processName"] in cmd:
                    return int(d)
        return None
    @staticmethod
    def getRemotePID(rs):
        #TODO implement a proper way to get the PID in a remote machine.
        return 0
    @staticmethod
    def system_call(command, background=False, outFile=None, errFile=None):
        cmds = command.split('|')
        stdin = subprocess.PIPE
        stdout = subprocess.PIPE
        stderr = subprocess.PIPE
        if outFile != None:
            stdout = open(outFile, 'w+')
            stderr = stdout
        if errFile != None:
            stderr = open(errFile, 'w+')
        for cmd in cmds:
            args = shlex.split(cmd)
            #print args
            p = subprocess.Popen(args, stdin=stdin, stdout=stdout, stderr=stderr)
            if not background:
                wait = 1
                step = 0.1
                total = 0
                conv = 1/step
                #Maybe ceil() is required instead of int()
                for i in range(0, int(wait*conv)):
                    if p.poll() != None:
                        break
                    time.sleep(step)
                    total += step
                if total >= wait-step:
                    p.terminate()
                    time.sleep(step)
                    if p.poll() != None:
                        p.kill()
                    return [None, None, None]
                else:
                    if cmd == cmds[-1]:
                        [out, err] = p.communicate()
                        rc = p.returncode
                    else:
                        stdin = p.stdout
        if not background:
            return [out, rc, err]
        return [None, None, None]


    def __init__(self):
        self.limits = []
        self.deps = []
        self.json = {}
    def printContents(self, ind=''):
        print(ind + self.json["name"])
        for rl in self.limits:
            rl.printContents(ind + '   ')
    def addDep(self, dep):
        self.deps.append(dep)
        for rl in self.limits:
            rl.addDep(dep)
    def setName(self, name):
        self.name = name
    def setContent(self, json):
        self.json = json
    def addResourceLimit(self, rl):
        self.limits.append(rl)
    def checkResources(self):
        rls = []
        for rl in self.limits:
            status = rl.checkResource()
            if status:
                rls.append(rl)
        ResourceLimit.delegateTasks(rls)

class ResourceDaemon:
    def __init__(self):
        self.exit = False
        self.resources = []
        self.deps = []
        pass
    def config(self, cfg):
        f = open(cfg, 'r')
        data = json.load(f)
        f.close()
        for i in data["resources"]:
             r = Resource()
             self.deps.append(r)
             if i["machine"] == os.getenv("HOST"):
             	self.resources.append(r)
             r.setContent(i)
             for j in i["limits"]:
                 if j["type"] == "Memory":
                     rl = MemoryLimit()
                 else:
                     print("Unknown resource limit type, might raise an exception")
                     rl = ResourceLimit()
                 rl.setContent(j)
                 rl.setResource(r)
                 r.addResourceLimit(rl)
                 for k in j["tasks"]:
                     t = None
                     if k["type"] == "Kill":
                         t = KillTask()
                     elif k["type"] == "Email":
                         t = EmailTask()
                     else:
                         print("Unknown task type, might raise an exception")
                         t = Task()
                     t.setContent(k)
                     t.setResource(r)
                     rl.addTask(t)
        for r in self.resources:
            for d in r.json["deps"]:
                for rd in self.deps:
                    if d == rd.json["name"]:
                        r.addDep(rd)
    def run(self):
        print("Resources List")
        for r in self.resources:
            r.printContents('   ')
        while not self.exit:
            try:
                time.sleep(1)
                try:
                    print("Loop")
                    for r in self.resources:
                        r.checkResources()
                except Exception as e:
                    if isinstance(e, ExitException):
                        raise e
                    print("Unexpected exception: ", sys.exc_info()[0])
                    print(traceback.format_exc())
            except ExitException:
                self.finish()
    def finish(self):
        self.exit = True

def handler(signum, frame):
    print("Exiting...")
    raise ExitException()

if __name__ == "__main__":
    rd = ResourceDaemon()
    signal.signal(signal.SIGINT, handler);
    acsdata = os.getenv("ACSDATA")
    print(acsdata)
    if acsdata == None:
        sys.exit(1)
    file = acsdata+"/config/limits.conf"
    if not os.path.isfile(file):
        sys.exit(2)
    rd.config(file)
    rd.run()
