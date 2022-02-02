import simplejson as json
import traceback

import tornado.ioloop
import tornado.web
import tornado.gen
import tornado.concurrent

from concurrent.futures import ThreadPoolExecutor

import threading

import ACS__POA

from Acspy.Servants.ContainerServices import ContainerServices
from Acspy.Servants.ComponentLifecycle import ComponentLifecycle
from Acspy.Servants.ACSComponent import ACSComponent

from .JSONUtil import AcsJsonEncoder, AcsJsonDecoder


class ComponentCallerAPI(ACS__POA.ACSComponent,
                         ACSComponent,
                         ContainerServices,
                         ComponentLifecycle):

    def __init__(self):
        ACSComponent.__init__(self)
        ContainerServices.__init__(self)

        self.service = TornadoThread(self)
        self.service.daemon = True

    def initialize(self):
        self.service.start()

    def cleanUp(self):
        tornado.ioloop.IOLoop.current().stop()


class TornadoThread(threading.Thread):
    def __init__(self, services):
        threading.Thread.__init__(self)
        self.services = services

    def run(self):
        app = tornado.web.Application([
            (r"/", CorbaHandler, {'services': self.services}),
        ])

        # TODO: parametrize server port using CDB
        LISTENING_IP = '0.0.0.0'
        LISTENING_PORT = 9000

        app.listen(LISTENING_PORT)

        print(('== Server listening in {0}:{1}'.format(LISTENING_IP, LISTENING_PORT)))
        tornado.ioloop.IOLoop.current().start()


# -----------------------------------------------
#                   Handlers
# -----------------------------------------------

class JsonHandler(tornado.web.RequestHandler):
    def set_default_headers(self):
        self.set_header('Content-Type', 'application/json')

    def prepare(self):
        if self.request.body:
            try:
                json_data = json.loads(self.request.body)
                self.request.arguments.update(json_data)
            except ValueError:
                raise tornado.web.HTTPError(
                    status_code=400, reason='Unable to parse JSON body')

    def write_error(self, status_code, **kwargs):
        if "exc_info" in kwargs:

            # in debug mode, try to send a traceback
            lines = []

            for line in traceback.format_exception(*kwargs["exc_info"]):
                lines.append(line)

            self.finish(json.dumps({
                'error': {
                    'code': status_code,
                    'message': self._reason,
                    'traceback': lines,
                }
            }))

        else:
            self.finish(json.dumps({
                'error': {
                    'code': status_code,
                    'message': self._reason,
                }
            }))

    def write_json(self, data, status_code=200):
        self.set_status(status_code)
        self.write(json.dumps(obj=data, cls=AcsJsonEncoder))


class CorbaHandler(JsonHandler):
    executor = ThreadPoolExecutor()

    def initialize(self, services):
        self.services = services

    @tornado.concurrent.run_on_executor
    def corba_invoke(self, component_name, method_name, arguments):

        params = json.loads(s=json.dumps(arguments), cls=AcsJsonDecoder)
        corba_params = []

        for key, value in params.items():
            corba_params.append(value)

        corba_component = self.services.getComponentNonSticky(component_name)
        corba_method = getattr(corba_component, method_name)

        return corba_method(*corba_params)

    @tornado.gen.coroutine
    def post(self):
        if not self.request.body:
            raise tornado.web.HTTPError(
                status_code=400, reason='Invalid arguments provided')

        body = json.loads(self.request.body)

        try:
            component_name = str(body['componentName'])
            method_name = str(body['methodName'])
            arguments = json.loads(str(body['arguments']))
        except:
            raise tornado.web.HTTPError(
                status_code=400, reason='Invalid request format')

        corba_response = yield self.corba_invoke(component_name, method_name, arguments)

        self.write_json(data={
            'data': corba_response
        })
