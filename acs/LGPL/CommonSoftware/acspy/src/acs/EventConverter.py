from Acspy.Servants.ContainerServices import ContainerServices
from Acspy.Servants.ComponentLifecycle import ComponentLifecycle
from Acspy.Servants.ACSComponent import ACSComponent
from Acspy.Nc.Consumer import Consumer

from xml.dom.minidom import parseString
from importlib import import_module
from acs.JSONUtil import AcsJsonEncoder

import ACS__POA

import traceback
import json
import redis


def __create_pubsub_handler__(redis_ref, channel_name):
    """
    Creates a handler according to the Notification channel client API

    :param redis_ref: the reference to redis connection
    :param channel_name: the name of the channel to create a handler function
    :return: the function reference to a notification channel handler
    """
    def _handler(event):
        """
        This event handler converts the event into JSON

        :param event: the actual event
        """
        redis_ref.publish(channel_name, json.dumps(obj=event, cls=AcsJsonEncoder))

    return _handler

def __create_reliable_subscriber_handler__(redis_ref, rs_name, options):
    """
    Creates a handler according to the Notification channel client API

    :param redis_ref: the reference to redis connection
    :param channel_name: the name of the channel to create a handler function
    :return: the function reference to a notification channel handler
    """
    queue_limit = int(options.get('queueLimit')) if options.get('queueLimit') else 10000

    def _handler(event):
        """
        This event handler converts the event into JSON

        :param event: the actual event
        """

        redis_ref.lpush(rs_name, json.dumps(obj=event, cls=AcsJsonEncoder))

        # check defined queue limit and trim it if exceeded
        queue_length = redis_ref.llen(rs_name)

        if queue_length > queue_limit:
            redis_ref.ltrim(rs_name, 0, queue_limit - 1)

    return _handler

class EventConverter(ACS__POA.ACSComponent,
                     ACSComponent,
                     ContainerServices,
                     ComponentLifecycle):
    """
    ACS component intended to convert the ACS Notification Channel Events into redis pubsub messages

    The definition of what channels and event types can be setup if the ACS CDB XML Doc of this component e.g:
    ``/alma/EventConverter``. The following is an example of how the component should be configured:

        <?xml version="1.0" encoding="ISO-8859-1"?>
        <EventConverter xmlns="urn:schemas-cosylab-com:EventConverter:1.0"
                    xmlns:baci="urn:schemas-cosylab-com:BACI:1.0"
                    xmlns:cdb="urn:schemas-cosylab-com:CDB:1.0"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

            <RedisConn host="localhost" port="6379"/>
            <PubSub>
                <Channel name="CONTROL_SYSTEM">
                    <EventType type="Control.ExecBlockStartedEvent"/>
                    <EventType type="Control.ExecBlockEndedEvent"/>
                    <EventType type="Control.ASDMArchivedEvent"/>
                </Channel>
                <Channel name="ShiftlogScriptInformation">
                    <EventType type="obops.ScriptInformationEvent"/>
                </Channel>
                <Channel name="TelCalPublisherEventNC">
                    <EventType type="telcal.WVRReducedEvent"/>
                </Channel>
                <Channel name="SCHEDULING_CHANNEL">
                    <EventType type="scheduling.CreatedArrayEvent"/>
                    <EventType type="scheduling.DestroyedArrayEvent"/>
                    <EventType type="scheduling.StartSessionEvent"/>
                </Channel>
            </PubSub>
            <ReliableSubscribers>
                <Subscriber name="slt" queueLimit="10">
                    <Channel name="SCHEDULING_CHANNEL">
                        <EventType type="scheduling.CreatedArrayEvent"/>
                        <EventType type="scheduling.DestroyedArrayEvent"/>
                        <EventType type="scheduling.StartSessionEvent"/>
                    </Channel>
                    <Channel name="TelCalPublisherEventNC">
                        <EventType type="telcal.WVRReducedEvent"/>
                    </Channel>
                </Subscriber>
            </ReliableSubscribers>
        </EventConverter>

    The Event Converter component will listen the given Notification channel, then convert the events into JSON
    format with the following structure:
    * A meta section for metadata structures, like the ``type`` of the event, which can be used later to decode the
    JSON Object
    * A Data section contained the event converted to JSON format

    Finally the JSON Object is forwarded to redis using the same channel name defined in the configuration.

    If you want to receive the messages in redis, please review the Redis pubsub API.
    """

    def __init__(self):
        ACSComponent.__init__(self)
        ContainerServices.__init__(self)

        # Keeps track of consumer instances
        self._consumers = {}

        # Reference to redis connection
        self.redis_ref = None

    def _parse_reliable_subscribers_from_dom(self, dom):
        """
        Return datastruct to store for each Reliable Subscriber the events to be listened to each channel
        """

        parent_node = dom.getElementsByTagName('ReliableSubscribers')[0]

        if not parent_node:
            return {}

        reliable_subscribers = {}
        subscribers = parent_node.getElementsByTagName('Subscriber')

        for sub in subscribers:
            sub_name = sub.getAttribute('name')
            reliable_subscribers[sub_name] = {}

            sub_channels = sub.getElementsByTagName('Channel')

            for sub_channel in sub_channels:
                sub_channel_name = sub_channel.getAttribute('name')
                reliable_subscribers[sub_name][sub_channel_name] = []

                sub_events = sub_channel.getElementsByTagName('EventType')

                reliable_subscribers[sub_name][sub_channel_name] = list(map(lambda e: e.getAttribute('type'), sub_events))

        return reliable_subscribers

    def _parse_reliable_subscribers_options_from_dom(self, dom):
            """
            Return datastruct to store for each Reliable Subscriber the events to be listened to each channel
            """

            parent_node = dom.getElementsByTagName('ReliableSubscribers')[0]

            if not parent_node:
                return {}

            reliable_subscribers_options = {}
            subscribers = parent_node.getElementsByTagName('Subscriber')

            for sub in subscribers:
                sub_name = sub.getAttribute('name')
                reliable_subscribers_options[sub_name] = dict(filter(lambda x: x[0] != 'name', sub.attributes.items()))

            return reliable_subscribers_options

    def _parse_pubsub_from_dom(self, dom):
        """
        Return datastruct to store pubsub related subscriptions
        """

        parent_node = dom.getElementsByTagName('PubSub')[0]

        if not parent_node:
            return {}

        pubsub = {}

        sub_channels = parent_node.getElementsByTagName('Channel')

        for sub_channel in sub_channels:
            sub_channel_name = sub_channel.getAttribute('name')
            pubsub[sub_channel_name] = []

            sub_events = sub_channel.getElementsByTagName('EventType')

            pubsub[sub_channel_name] = list(map(lambda e: e.getAttribute('type'), sub_events))

        return pubsub


    def _get_acs_module_from_string(self, s):
        module_str_index = s.rfind('.')

        if module_str_index == 0:
            return None

        module_name = s[:module_str_index]

        return import_module(module_name)

    def _get_acs_class_from_string(self, s):
        class_str_index = s.rfind('.')

        if class_str_index == 0:
            return None

        acs_module = self._get_acs_module_from_string(s)

        return acs_module.__dict__[s[class_str_index + 1:]]

    def initialize(self):
        """
        Read configuration from CDB
        Initializes redis connection and consumers connecting them into the ACS Notification Channel
        """
        dom = parseString(self.getCDBRecord('alma/EventConverter'))

        redis_node = dom.getElementsByTagName('RedisConn')

        self.getLogger().logInfo("Connecting to rforwith following parameters host=%s, port=%s"
                                % (redis_node[0].getAttribute('host'),
                                   str(redis_node[0].getAttribute('port'))))

        self.redis_ref = redis.StrictRedis(host=redis_node[0].getAttribute('host'),
                                           port=redis_node[0].getAttribute('port'))


        self.getLogger().logInfo('Read CDB Node aforad configuration')


        # define pubsub subscriptions:
        pubsub = self._parse_pubsub_from_dom(dom)

        for channel_name, events in pubsub.items():
            if not channel_name in self._consumers:
                self._consumers[channel_name] = Consumer(channel_name)

            funczz = __create_pubsub_handler__(self.redis_ref, channel_name)

            for event in events:
                try:
                    clazz = self._get_acs_class_from_string(event)

                    self.getLogger().logInfo('[PubSub] Registering %s, %s' % (clazz, funczz))

                    self._consumers[channel_name].addSubscription(clazz, funczz)
                except Exception as e:
                    self.getLogger().logError("I have no idea how to handle a class outside a package. Help!")
                    traceback.print_exc()

        # define reliable subscriptions:
        reliable_subscribers = self._parse_reliable_subscribers_from_dom(dom)
        reliable_subscribers_options = self._parse_reliable_subscribers_options_from_dom(dom)

        for rs_name, subscriptions in reliable_subscribers.items():
            rs_options = reliable_subscribers_options.get(rs_name) or {}

            for channel_name, events in subscriptions.items():
                if not (rs_name + '_' + channel_name) in self._consumers:
                    self._consumers[rs_name + '_' + channel_name] = Consumer(channel_name)

                for event in events:
                    try:
                        clazz = self._get_acs_class_from_string(event)
                        # create a new event handler function per event
                        funczz = __create_reliable_subscriber_handler__(self.redis_ref, rs_name, rs_options)

                        self.getLogger().logInfo('[Reliable Subscribers] Registering for queue %s: %s, %s' % (rs_name, clazz, funczz))

                        self._consumers[rs_name + '_' + channel_name].addSubscription(clazz, funczz)
                    except Exception as e:
                        self.getLogger().logError("I have no idea how to handle a class outside a package. Help!")
                        traceback.print_exc()

        # Start all consumers:
        for consumer in self._consumers.values():
            consumer.consumerReady()

    def cleanUp(self):
        for consumer in self._consumers.values():
            consumer.disconnect()
