## env
### conda

python==3.6 \
tensorflow==1.12.0 \
keras==2.2.4 \
setproctitle \
pandas \
flask

### pip

obspy
kafka-python
pymysql

python2.7 -> 3:
xrange=range
print=print()


### modify code
```
# unet_eval_from_stream.py
import os
os.environ['KMP_DUPLICATE_LIB_OK']='True'
import sys
sys.path.append("/Users/cui/Desktop/unet_cea")

# unet.py
print 011, color_conv1_1
-->
print(0o11, color_conv1_1)
```

python ./bin/unet_eval_from_stream.py --stream_path ./mseed/ --checkpoint_path unet_capital/unet.ckpt-590000 --batch_size 8 --output_dir output/predict_from_stream --plot

## Update env

python=3.7
tensorflow=2.0.0