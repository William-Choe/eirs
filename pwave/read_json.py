import ast
from obspy import read
import io


event_path = "C:\\Users\\cui\\Desktop\\events\\"
event_file = "SF202106061557A-033C-01.json"
with open(event_path + event_file, 'r') as file:
    dic = dict(ast.literal_eval(file.read()))
    print(dic.keys())
    stream = read(io.BytesIO(dic['YN.HEQ.00']['waveform']))
    print(stream)
