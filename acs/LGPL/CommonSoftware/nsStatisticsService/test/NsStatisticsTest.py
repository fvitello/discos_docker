#!/usr/bin/env python
# @(#) $Id: NsStatisticsTest.py,v 1.4 2015/01/23 16:51:58 pcolomer Exp $
#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) Associated Universities Inc., 2002
# (c) European Southern Observatory, 2002
# Copyright by ESO (in the framework of the ALMA collaboration)
# and Cosylab 2002, All rights reserved
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA 02111-1307  USA
#
#------------------------------------------------------------------------------
import getpass
import re
import sys
import os

class AcsTestLogChecker(object):
    REL_PATH_FILE_PID="./tmp/pid_test"
    def __init__(self, num_proc, prefix, customLog=False):
        self.num_proc = int(num_proc)
        self.prefix = prefix

        if customLog: return

        self.pid = self.read_pid()
        self.user_name = getpass.getuser() # User name of the user executing the test
        self.log_file = self.read_log_file()

    def set_num_proc(self,num_proc):
        self.num_proc = num_proc

    def l(self,string):
        if not self.prefix:
            print(string)
        else:
            print("%s %s" % (self.prefix, string))

    def read_pid(self):
        pid = ""
        try:
            f = open(self.REL_PATH_FILE_PID, 'r')
            pid = f.read()
            f.close()
        except:
            self.l("Unexpected error reading pid file %s: %s" % (self.REL_PATH_FILE_PID,sys.exc_info()[0]))
            raise
        return pid

    def read_log_file(self):
        # Create path of tmp log file
        tmp_log = "/tmp/%s_test%s.%d"%(self.user_name,self.pid,self.num_proc)

        string = ""
        try:
            f = open(tmp_log, 'r')
            string = f.read()
            f.close()
        except:
            self.l("Unexpected error reading file %s: %s" % (tmp_log,sys.exc_info()[0]))
            raise

        return string

    def read_custom_log_file(self, path):
        # Create path of tmp log file

        string = ""
        try:
            f = open(path, 'r')
            string = f.read()
            f.close()
        except:
            self.l("Unexpected error reading file %s: %s" % (path, sys.exc_info()[0]))
            raise

        return string

    def find_pattern(self,pattern):
        p = re.compile(pattern)
        return p.findall(self.log_file)

    def check_pattern_n_times_in_range(self,pattern,min_occ,max_occ):
        m = self.find_pattern(pattern)
        if m is not None and min_occ <= len(m) and len(m) <= max_occ:
            self.l("OK - Number of instances of pattern '%s' is between the required range" % (pattern))
        else:
            self.l("FAIL - Found %d instances of pattern '%s' but range is [%d, %d]" % (len(m), pattern, min_occ, max_occ))
        return len(m)

    def check_pattern_exists_at_least(self,pattern,min_occ):
        m = self.find_pattern(pattern)
        if min_occ <= len(m):
            self.l("OK - Number of instances of pattern '%s' is equal or greater than the minimum required" % (pattern))
        else:
            self.l("FAIL - Found %d instances of pattern '%s' but the minimum is %d" % (len(m), pattern, min_occ))
        return len(m)

    def check_pattern_not_exists(self,pattern):
        m = self.find_pattern(pattern)
        if len(m) <= 0:
            self.l("OK - Pattern '%s' not found" % (pattern))
        else:
            self.l("FAIL - Pattern '%s' found %d times" % (pattern, len(m)))
        return len(m)

    def check_pattern_exists(self,pattern):
        m = self.find_pattern(pattern)
        if len(m) > 0:
            self.l("OK - Pattern '%s' found" % (pattern))
        else:
            self.l("FAIL - Pattern '%s' not found" % (pattern))
        return len(m)

def get_pattern_debug_stats_channel(channel_name):
    """
    string =  "DEBUG\ \[nsStatistics\]\s+DEBUG STATISTICS OF NOTIFICATION CHANNEL " + channel_name + "\n"
    string += "(\s+Supplier names.+)\n"
    string += "(\s+Consumer names.+)\n"
    string += "(\s+Supplier admin names.+)\n"
    string += "(\s+Consumer admin names.+)"
    """
    string =  "DEBUG\ \[nsStatistics\]\s+STATISTICS OF NOTIFICATION CHANNEL " + channel_name
    string += "\s+\[\s+"
    string += "("
    string += "Num suppliers\=\'\d+\'\s+|"
    string += "Num consumers\=\'\d+\'\s+|"
    string += "Num slowest consumers\=\'\d+\'\s+|"
    string += "Num suppliers admin\=\'\d+\'\s+|"
    string += "Num consumers admin\=\'\d+\'\s+|"
    string += "Current consumers=\'.*\'\s+|"
    string += "Current suppliers=\'.*\'\s+|"
    string += "Current slowest consumers=\'.*\'\s+|"
    string += "Current consumers admin=\'.*\'\s+|"
    string += "Current suppliers admin=\'.*\'\s+|"
    string += "All consumers=\'.*\'\s+|"
    string += "All suppliers=\'.*\'\s+|"
    string += "All slowest consumers=\'.*\'\s+|"
    string += "All consumers admin=\'.*\'\s+|"
    string += "All suppliers admin=\'.*\'\s+|"
    string += "){5,15}"
    string += "\]"

    return string

def get_pattern_stats_channel(channel_name,tqs=False,toe=False):
    string =  "INFO\ \[nsStatistics\]\s+STATISTICS OF NOTIFICATION CHANNEL " + channel_name
    string += "\s+\[\s+"
    string += "("
    string += "Num consumers=\'MIN=\d+, MAX=\d+, AVG=\d+(\.\d+)?\'\s+|"
    string += "Avg events in queues=\'(\d+(\.\d+)?,\s)*\'\s+|"
    string += "Max events in queues=\'(\d+,\s)*\'\s+|"
    string += "Num suppliers=\'MIN=\d+, MAX=\d+, AVG=\d+(\.\d+)?\'\s+|"
    #string += "Num suppliers\=\'\w+\'\s+|"
    #string += "Num consumers\=\'\w+\'\s+|"
    #string += "Num events in queues=\'(\d+(\,\s)?)*\'\s+|"
    if tqs:
        string += "Max size of queues \[bytes\]=\'.*\'\s+|"
        string += "Avg size of queues \[bytes\]=\'.*\'\s+|"
    if toe:
        string += "Oldest event=\'\d+\'\s+|"
    string += "Current slowest consumers=\'.*\'\s+|"
    string +="All slowest consumers=\'.*\'\s+"
    string += "){4,6}"
    string += "\]"

    return string

"""
def get_pattern_stats_channel_with_tqs(channel_name):
    string = get_pattern_stats_channel(channel_name)
    string += "\n(\s+Size of queues in bytes\:(\s\d+\,?)*)"
    return string

def get_pattern_stats_channel_with_toe(channel_name):
    string = get_pattern_stats_channel(channel_name)
    string += "\n(\s+Oldest event\: \w*)"
    return string
"""

def get_pattern_stats_factory(factory_name):
    """
    string =  "INFO\ \[nsStatistics\]\s+STATISTICS OF NOTIFICATION FACTORY " + factory_name + "\n"
    string += "(\s+Active event channels \[\d+\])\n"
    string += "((\s+.+)\n)*"
    """
    string =  "INFO\ \[nsStatistics\]\s+STATISTICS OF NOTIFICATION FACTORY " + factory_name
    string += "\s+\[\s+"
    string += "("
    string += "Active event channels\=\'.*\'\s+|"
    string += "Num active event channels\=\'\d+\'\s+"
    string += "){1,2}"
    string += "\]"
    return string

def get_pattern_warn_freq():
    string = "WARNING \[nsStatistics\] Statistics of Notification Services will"
    string += " be obtained with a very high frequency \(less than 1 minute time interval\)"
    return string

def test_case1():
    min_stats = 1
    max_stats = 2
    test = AcsTestLogChecker(2,"Test1")

    test.check_pattern_not_exists(get_pattern_warn_freq())

    # Check factories
    n = test.check_pattern_n_times_in_range(get_pattern_stats_factory("Alarm"), min_stats, max_stats)
    min_val = 1 if n == 1 else (n - 1)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("Archive"), min_val, n + 1)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("Logging"), min_val, n + 1)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("DefaultNotifyService"), min_val, n + 1)

    # Check channels
    test.check_pattern_n_times_in_range(get_pattern_stats_channel("Logging#LoggingChannel"), min_val, n)
    test.check_pattern_n_times_in_range(get_pattern_debug_stats_channel("Logging#LoggingChannel"), min_val, n)
    test.check_pattern_n_times_in_range(get_pattern_stats_channel("Archive#ArchivingChannel"), min_val, n)
    test.check_pattern_n_times_in_range(get_pattern_debug_stats_channel("Archive#ArchivingChannel"), min_val, n)
    test.check_pattern_exists_at_least(get_pattern_stats_channel("DefaultNotifyService#testNsStatsChannel1", True, True), min_val) # min_stats/2)
    test.check_pattern_exists_at_least(get_pattern_debug_stats_channel("DefaultNotifyService#testNsStatsChannel1"), min_val) # min_stats/2)
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL Alarm")


def test_case2():
    min_stats = 3
    max_stats = 15
    test = AcsTestLogChecker(2,"Test2")

    test.check_pattern_exists(get_pattern_warn_freq())

    # Check factories
    n = test.check_pattern_n_times_in_range(get_pattern_stats_factory("DefaultNotifyService"), min_stats, max_stats)
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION FACTORY Alarm")
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION FACTORY Archive")
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION FACTORY Logging")

    # Check channels
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL Logging#LoggingChannel")
    test.check_pattern_not_exists("DEBUG [nsStatistics] STATISTICS OF NOTIFICATION CHANNEL Logging#LoggingChannel")
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL Archive#ArchivingChannel")
    test.check_pattern_not_exists("DEBUG [nsStatistics] STATISTICS OF NOTIFICATION CHANNEL Archive#ArchivingChannel")
    test.check_pattern_exists_at_least(get_pattern_stats_channel("DefaultNotifyService#testNsStatsChannel1", True, True), min_stats/2)
    test.check_pattern_exists_at_least(get_pattern_debug_stats_channel("DefaultNotifyService#testNsStatsChannel1"), min_stats/2)
    test.check_pattern_exists_at_least(get_pattern_stats_channel("DefaultNotifyService#testNsStatsChannel2", True, True), min_stats/2)
    test.check_pattern_exists_at_least(get_pattern_debug_stats_channel("DefaultNotifyService#testNsStatsChannel2"), min_stats/2)
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL Alarm")

def test_case3():
    min_stats = 3
    max_stats = 25
    test = AcsTestLogChecker(2,"Test3")

    test.check_pattern_exists(get_pattern_warn_freq())

    # Check factories
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION FACTORY Alarm")
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION FACTORY Archive")
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION FACTORY Logging")
    n = test.check_pattern_n_times_in_range(get_pattern_stats_factory("DefaultNotifyService"), min_stats, max_stats)

    # Check channels
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL Logging#LoggingChannel")
    test.check_pattern_not_exists("DEBUG [nsStatistics] STATISTICS OF NOTIFICATION CHANNEL Logging#LoggingChannel")
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL Archive#ArchivingChannel")
    test.check_pattern_not_exists("DEBUG [nsStatistics] STATISTICS OF NOTIFICATION CHANNEL Archive#ArchivingChannel")
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL DefaultNotifyService#testNsStatsChannel1")
    test.check_pattern_not_exists("DEBUG [nsStatistics] STATISTICS OF NOTIFICATION CHANNEL DefaultNotifyService#testNsStatsChannel1")
    test.check_pattern_exists_at_least(get_pattern_stats_channel("DefaultNotifyService#testNsStatsChannel2",True,True), min_stats/2)
    test.check_pattern_exists_at_least(get_pattern_debug_stats_channel("DefaultNotifyService#testNsStatsChannel2"), min_stats/2)
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL Alarm")

def test_case4():
    min_stats = 4
    max_stats = 25
    test = AcsTestLogChecker(0,"Test4")

    test.check_pattern_exists(get_pattern_warn_freq())

    # Check factories
    n = test.check_pattern_n_times_in_range(get_pattern_stats_factory("Alarm"), min_stats, max_stats)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("Archive"), n - 1, n)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("Logging"), n - 1, n)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("DefaultNotifyService"), n - 1, n)

    # Check channels
    test.check_pattern_n_times_in_range(get_pattern_stats_channel("LoggingChannel",toe=True), n - 1, n)
    test.check_pattern_n_times_in_range(get_pattern_debug_stats_channel("LoggingChannel"), n - 1, n)
    test.check_pattern_n_times_in_range(get_pattern_stats_channel("ArchivingChannel",toe=True), n - 1, n)
    test.check_pattern_n_times_in_range(get_pattern_debug_stats_channel("ArchivingChannel"), n - 1, n)
    test.check_pattern_exists_at_least(get_pattern_stats_channel("testNsStatsChannel1",toe=True), min_stats/2)
    test.check_pattern_exists_at_least(get_pattern_debug_stats_channel("testNsStatsChannel1"), min_stats/2)
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL Alarm")


def test_case5():
    min_stats = 4
    max_stats = 25
    test = AcsTestLogChecker(0,"Test5")

    test.check_pattern_exists(get_pattern_warn_freq())

    # Check factories
    n = test.check_pattern_n_times_in_range(get_pattern_stats_factory("Alarm"), min_stats, max_stats)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("Archive"), n - 1, n)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("Logging"), n - 1, n)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("DefaultNotifyService"), n - 1, n)

    # Check channels
    test.check_pattern_n_times_in_range(get_pattern_stats_channel("LoggingChannel",tqs=True), n - 1, n)
    test.check_pattern_n_times_in_range(get_pattern_debug_stats_channel("LoggingChannel"), n - 1, n)
    test.check_pattern_n_times_in_range(get_pattern_stats_channel("ArchivingChannel",tqs=True), n - 1, n)
    test.check_pattern_n_times_in_range(get_pattern_debug_stats_channel("ArchivingChannel"), n - 1, n)
    test.check_pattern_exists_at_least(get_pattern_stats_channel("testNsStatsChannel1",tqs=True), min_stats/2)
    test.check_pattern_exists_at_least(get_pattern_debug_stats_channel("testNsStatsChannel1"), min_stats/2)
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL Alarm")

def test_case6():
    min_stats = 1
    max_stats = 2
    test = AcsTestLogChecker(6, "Test6", True)

    test.log_file = test.read_custom_log_file(os.path.join(os.environ.get("ACS_TMP"), "NsStatisticsComponentTest_frodoContainer.log"))

    test.check_pattern_not_exists(get_pattern_warn_freq())

    # Check factories
    n = test.check_pattern_n_times_in_range(get_pattern_stats_factory("Alarm"), min_stats, max_stats)
    min_val = 1 if n == 1 else (n - 1)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("Archive"), min_val, n + 1)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("Logging"), min_val, n + 1)
    test.check_pattern_n_times_in_range(get_pattern_stats_factory("DefaultNotifyService"), min_val, n + 1)

    # Check channels
    test.check_pattern_n_times_in_range(get_pattern_stats_channel("Logging#LoggingChannel"), min_val, n)
    test.check_pattern_n_times_in_range(get_pattern_debug_stats_channel("Logging#LoggingChannel"), min_val, n)
    test.check_pattern_n_times_in_range(get_pattern_stats_channel("Archive#ArchivingChannel"), min_val, n)
    test.check_pattern_n_times_in_range(get_pattern_debug_stats_channel("Archive#ArchivingChannel"), min_val, n)
    test.check_pattern_exists_at_least(get_pattern_stats_channel("DefaultNotifyService#testNsStatsChannel1", True, True), min_val) # min_stats/2)
    test.check_pattern_exists_at_least(get_pattern_debug_stats_channel("DefaultNotifyService#testNsStatsChannel1"), min_val) # min_stats/2)
    test.check_pattern_not_exists("STATISTICS OF NOTIFICATION CHANNEL Alarm")


def main(argv):
    test_case = int(argv[0])
    if test_case == 1:
        test_case1()
    elif test_case == 2:
        test_case2()
    elif test_case == 3:
        test_case3()
    elif test_case == 4:
        test_case4()
    elif test_case == 5:
        test_case5()
    elif test_case == 6:
        test_case6()
    else:
        raise BaseException("Unknown test case")

if __name__ == "__main__":
    main(sys.argv[1:])
