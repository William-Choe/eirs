# P波拾取模型
unet_checkpoint_path = "./model/unet.ckpt-590000"
unet_pd_path = "./model/unet_590000.pb"
unet_batch_size = 1
unet_image_size = 3001

# Kafka-EIRS
eirs_bootstrap_servers = ["8.131.255.216:9092"]
eirs_topic_process_pwave = "p-wave-info"
eirs_group_id_process_pwave = "process-pwave"
eirs_group_id_save_pwave = "save-pwave"

# Kafka-Receive Data
data_bootstrap_servers = ["10.5.107.10:9092", "10.5.107.11:9092", "10.5.107.38:9092", "10.5.107.123:9092", "10.5.66.224:9092"]
data_topic = "mseed-caps-java-2.0"
data_group_id = "eirs_test"

# MySQL
mysql_host = "rm-2ze78q57mdh214rk0125010hm.mysql.rds.aliyuncs.com"
mysql_user = "seis"
mysql_password = "Neu@2021"
mysql_database = "eirs"

# P波存储路径
pwave_save_path = "C:\\Users\\cui\\Desktop\\p_waves"

# 接收数据路径
data_path = "C:\\Users\\cui\\Desktop\\data"
