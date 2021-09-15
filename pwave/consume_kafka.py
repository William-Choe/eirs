from kafka import KafkaConsumer
import logging
import json
import ast
from obspy import read
import io

event_path = "C:\\Users\\cui\\Desktop\\events\\"


def main():

    consumer = KafkaConsumer('events',
                             bootstrap_servers=["8.131.255.216:9092"],
                             value_deserializer=lambda m: json.loads(m.decode('utf-8'))
                             )
    for message in consumer:
        print("-> receive message")
        if message:
            try:
                event_file = message.value
                with open(event_path + event_file, 'r') as file:
                    dic = dict(ast.literal_eval(file.read()))
                    st = read(io.BytesIO(dic['YN.HEQ.00']['waveform']))
                    print(dic.keys())
                    print(st)
            except Exception as e:
                logging.error(e)


if __name__ == '__main__':
    main()
