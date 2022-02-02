#!/usr/bin/env python
from __future__ import print_function
import FRIDGE
import json
from acs.JSONUtil import AcsJsonEncoder, AcsJsonDecoder


ev = FRIDGE.temperatureDataBlockEvent(0.0001, FRIDGE.OVERREF)
print('obj', ev)
print('dict',ev.__dict__)
json_str = json.dumps(obj=ev, cls=AcsJsonEncoder)
print(json_str)
obj_p = json.loads(s=json_str, cls=AcsJsonDecoder)
print('obj', obj_p)
print('dict',obj_p.__dict__)


import ACSErr

et = ACSErr.ErrorTrace('myFile', 10, 'myRoutine', 'MyHost', 'pid', 'thread', 1000000, 'obj', 0, 0, ACSErr.Error, 'desc', ACSErr.NameValue('myName', 'myValue'), [])
et = ACSErr.ErrorTrace('myFile2', 20, 'myRoutine2', 'MyHost2', 'pid2', 'thread2', 1100000, 'obj2', 1, 1, ACSErr.Error, 'desc2', ACSErr.NameValue('myName2', 'myValue2'), [et])
print('obj', et)
print('dict',et.__dict__)
json_str = json.dumps(obj=et, cls=AcsJsonEncoder)
print(json_str)
obj_p = json.loads(s=json_str, cls=AcsJsonDecoder)
print('obj', obj_p)
print('dict',obj_p.__dict__)

