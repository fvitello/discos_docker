#! /usr/bin/env python
import time
import datetime
import acscommon
import traceback
import NotifyMonitoringExt
import CosNotifyChannelAdmin

from ACSErr import NameValue
from Acspy.Util import NameTree
from Acspy.Util.ACSCorba import cdb
from Acspy.Util.ACSCorba import getORB
from Acspy.Util.ACSCorba import getManager
from Acspy.Common.CDBAccess import CDBaccess
from ACSErrTypeCommonImpl import CORBAProblemExImpl
from AcsutilPy.WildcharMatcher import wildcharMatch

from acscommon import NC_KIND_NCSUPPORT
from acscommon import NAMESERVICE_BINDING_NC_DOMAIN_DEFAULT
from acscommon import NAMESERVICE_BINDING_NC_DOMAIN_SEPARATOR

from Acspy.Nc.CDBProperties import get_channel_qofs_props
from Acspy.Nc.CDBProperties import get_channel_admin_props
from Acspy.Nc.CDBProperties import cdb_channel_config_exists
from Acspy.Nc.CDBProperties import get_notification_service_mapping

def configQoS(channelName):
    if cdb_channel_config_exists(channelName):
        return get_channel_qofs_props(channelName)
    else:
        return []
def configAdminProps(channelName):
    if cdb_channel_config_exists(channelName):
        return get_channel_admin_props(channelName)
    else:
        return []

def getNotificationFactoryNameForChannel(channel,domain=None):
    temp=""
    if channel is not None:
        crec = [ chan for chan in get_notification_service_mapping('Channel') if wildcharMatch(chan['Name'], channel)]
        if crec != []:
            temp=crec[0]['NotificationService']

    if len(temp)==0 and (domain is not None):
        crec = [ chan for chan in get_notification_service_mapping('Domain') if wildcharMatch(chan['Name'], domain)]
        if crec != []:
            temp=crec[0]['NotificationService']

    if len(temp)==0:
        crec = get_notification_service_mapping('Default')
        if crec != []:
            temp=crec[0]['DefaultNotificationService']
        else:
            return None

    if not temp.endswith(acscommon.NOTIFICATION_FACTORY_NAME):
        return temp+acscommon.NOTIFICATION_FACTORY_NAME
    else:
        return temp

def getChannelKind():
    return acscommon.NC_KIND

def combineChannelAndDomainName(channelName, domainName):
    return channelName+NAMESERVICE_BINDING_NC_DOMAIN_SEPARATOR+domainName

def getChannelTimestampKind():
    return NC_KIND_NCSUPPORT

def setChannelTimestamp(nt, eventChannel, channelName, domainName):
    ts = time.time()
    st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d_%H:%M:%S')
    channel_name_timestamp = combineChannelAndDomainName(channelName, domainName) + "-" + st

    try:
        nt.putObject(channel_name_timestamp, getChannelTimestampKind(), eventChannel)
        channelTimestamp = datetime.datetime.fromtimestamp(ts)
        return True
    except Exception as e:
        print("Failed to bind the timestamp of the channel '%s'"%(channelName))
        traceback.print_exc()
        return False

def createNotificationChannel(channelName, domainName=None):
    try:
        nt = NameTree.nameTree(getORB())
    except Exception as e:
        raise CORBAProblemExImpl(nvSeq=[NameValue("channelname", channelName), NameValue("exception", str(e))])
    if nt == None:
        raise CORBAProblemExImpl(nvSeq=[NameValue("channelname", channelName), NameValue("reason", "Naming Service")])

    try:
        factoryName = getNotificationFactoryNameForChannel(channelName, domainName) or acscommon.NOTIFICATION_FACTORY_NAME
        channelFactory = nt.getObject(factoryName, "")
        channelFactory = channelFactory._narrow(NotifyMonitoringExt.EventChannelFactory)
    except Exception as e:
        raise CORBAProblemExImpl(nvSeq=[NameValue("channelname", channelName), NameValue("reason", "Unable to get Notification Service"), NameValue("exception", str(e))])

    try:
        (eventChannel, chan_id) = channelFactory.create_named_channel(configQoS(channelName), configAdminProps(channelName), channelName)
        chan_id = None
    except AttributeError as e:
        raise CORBAProblemExImpl(nvSeq=[NameValue("channelname", channelName), NameValue("reason", "Invalid channel factory"), NameValue("exception", str(e))])
    except Exception as e:
        raise CORBAProblemExImpl(nvSeq=[NameValue("channelname", channelName), NameValue("reason", "Unable to create channel"), NameValue("exception", str(e))])

    try:
        nt.putObject(combineChannelAndDomainName(channelName, domainName), getChannelKind(), eventChannel)
        n_attempts = 10
        timestamp_created = setChannelTimestamp(nt, eventChannel, channelName, domainName)
        while timestamp_created == False and n_attempts > 0:
           time.sleep(2)
           timestamp_created = setChannelTimestamp(nt, eventChannel, channelName, domainName)
           n_attempts -= 1

        if timestamp_created == False:
            print("Failed to register the timestamp of the channel '%s' after %d attempts. Subscribers will not be able to reconnect"%(channelName, n_attempts))
    except Exception as e:
        raise CORBAProblemExImpl(nvSeq=[NameValue("channelname", channelName), NameValue("reason", "Cannot register with Naming Service"), NameValue("exception", str(e))])
    return eventChannel

def setQoS(channelName, domainName=None):
    try:
        nt = NameTree.nameTree(getORB())
    except Exception as e:
        raise CORBAProblemExImpl(nvSeq=[NameValue("channelname", channelName), NameValue("exception", str(e))])
    if nt == None:
        raise CORBAProblemExImpl(nvSeq=[NameValue("channelname", channelName), NameValue("reason", "Naming Service")])
    try:
        #nt.ls()
        factoryName = getNotificationFactoryNameForChannel(channelName, domainName) or acscommon.NOTIFICATION_FACTORY_NAME
        channelFactory = nt.getObject(factoryName, "")
        channelFactory = channelFactory._narrow(NotifyMonitoringExt.EventChannelFactory)
        try:
            obj = nt.getObject(combineChannelAndDomainName(channelName, domainName), getChannelKind())
            eventChannel = obj._narrow(NotifyMonitoringExt.EventChannel)
        except Exception as e:
            print("Channel '" + channelName + "' on domain '" + domainName + "' does not exist, creating it")
            eventChannel = createNotificationChannel(channelName, domainName)
    except Exception as e:
        print(str(e))
        raise
    eventChannel.set_qos(configQoS(channelName))
    eventChannel.set_qos(configAdminProps(channelName))
    print("QoS has been set for channel '" + channelName + "'.")

def getChannels():
    dal = cdb()
    channels = dal.list_nodes("MACI/Channels").strip().split()
    mapping = {}
    for ch in channels:
        mapping[ch] = getDomain(ch)
    return mapping

def getDomain(channelName):
    if wildcharMatch('LoggingChannel*', channelName):
        return 'LOGGING'
    if wildcharMatch('ArchivingChannel*', channelName):
        return 'ARCHIVING'
    if wildcharMatch('CMW.ALARM_SYSTEM.*', channelName):
        return 'ALARMSYSTEM'
    ch = [ch for ch in get_notification_service_mapping('Channel') if wildcharMatch(ch['Name'], channelName)]
    if len(ch) == 0:
        return NAMESERVICE_BINDING_NC_DOMAIN_DEFAULT
    dm = [dm for dm in get_notification_service_mapping('Domain') if dm['NotificationService'] == ch['NotificationService']]
    if len(dm) == 0:
        return NAMESERVICE_BINDING_NC_DOMAIN_DEFAULT
    return dm['Name']

if __name__== "__main__":
    mgr = getManager()
    if mgr is None:
        print("acsConfigureNotificationChannels should be executed with the Manager running.")
    else:
        channels = getChannels()
        for ch in list(channels.keys()):
            setQoS(ch, channels[ch])
