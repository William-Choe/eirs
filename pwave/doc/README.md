# Seismic Data Receiving and P-wave Picking System

## Version 1

这部分需要运行前三个py。

- receive_seis_data.py: 接收台网Kafka集群数据包，存储至本地
- main_process.py: 处理本地存储的数据包，只负责到P波拾取，向Kafka发送“station:pwave-arrival-time”格式字符串
- process_pwave.py: 后续流程处理，消费上一个py发送的消息
- config.py: 子系统配置文件



## Version 2

使用台网中心提供的P波到时及定位信息，只需要运行下面这个py文件即可。

-   receive_event.py: 先接受台网中心Kafka集群的事件信息，按照事件各个台站的P波到时再从Redis中获取波形数据，事件信息和波形都存储在了json文件里，Kafka发送该json文件名