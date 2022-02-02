import simplejson as json
import omniORB
import omniORB.CORBA
from importlib import import_module


class AcsJsonEncoder (json.JSONEncoder):
    """
    Class used by ACS to identify CORBA Types and how to convert them into JSON
    The following classes are handled:
    * ``omniORB.StructBase`` and ``omniORB.CORBA.Object`` use their internal dictionaries
    * ``omniORB.EnumItem`` use its internal representation (``o.__repr__()``).

    """
    def default(self, o):
        if o is not None:
            if isinstance(o, omniORB.EnumItem):
                meta = {"type": "%s.%s" % (type(o).__module__, type(o).__name__)}
                return {
                    "meta": meta,
                    "data":o.__dict__
                }
            elif isinstance(o, omniORB.StructBase) or isinstance(o, omniORB.CORBA.Object):
                meta = {"type": "%s.%s" % (type(o).__module__, type(o).__name__)}
                return {
                    "meta": meta,
                    "data": o.__dict__
                }
        else:
            return json.JSONEncoder.default(self, o)


class AcsJsonDecoder(json.JSONDecoder):
    def __init__(self, *args, **kargs):
        json.JSONDecoder.__init__(self, object_hook=self.object_hook, *args, **kargs)

    def object_hook(self, obj):
        if 'meta' not in obj:
            if isinstance(obj, str):
                return str(obj)
            return obj
        if 'type' not in obj['meta']:
            return obj
        c_type = obj.pop('meta')['type']
        module = import_module(c_type[:c_type.rfind('.')])
        clazz = module.__dict__[c_type[c_type.rfind('.') + 1:]]
        data = obj.pop('data')
        if omniORB.EnumItem in clazz.mro():
            idl_id = data['_parent_id'].split(':')[1]
            if idl_id.startswith('alma/'):
                idl_id = idl_id[5:]
            c_type = idl_id.replace('/','.')
            module = import_module(c_type[:c_type.rfind('.')])
            clazz = module.__dict__[c_type[c_type.rfind('.') + 1:]]
            return clazz._items[data['_v']]
        elif omniORB.StructBase in clazz.mro() or omniORB.CORBA.Object in clazz.mro():
            clazz_args = {}
            for key, value in data.items():
                if isinstance(value, dict):
                    clazz_args[key] = json.loads(s=value, cls=AcsJsonDecoder)
                else:
                    clazz_args[key] = str(value)
            return clazz(**clazz_args)

