import pymysql
import config


class DB:
    def __init__(self):
        self.db = pymysql.connect(host=config.mysql_host, user=config.mysql_user,
                                  password=config.mysql_password, database=config.mysql_database)

    def getPWaveConfig(self):
        cursor = self.db.cursor()
        cursor.execute("select pwave_before, pwave_after from config")
        data = list(cursor.fetchone())

        pwave_before = data[0]
        pwave_after = data[1]

        cursor.close()

        return pwave_before, pwave_after

    def insert_p(self, coordinate, p_timestamp):
        cursor = self.db.cursor()
        cursor.execute("insert into catalog(coordinate, p_time, save_time) values ('{}', FROM_UNIXTIME({},'%Y-%m-%d %H:%i:%s'), now())".format(coordinate, p_timestamp))
        self.db.commit()

        cursor.execute("SELECT LAST_INSERT_ID()")
        id = cursor.fetchone()[0]
        cursor.close()

        return id
