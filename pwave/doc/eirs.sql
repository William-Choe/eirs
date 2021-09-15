SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for catalog
-- ----------------------------
DROP TABLE IF EXISTS `catalog`;
CREATE TABLE `catalog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `coordinate` varchar(255) DEFAULT NULL COMMENT '台站信息',
  `p_time` datetime DEFAULT NULL COMMENT 'p波到时',
  `save_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '存储时间',
  `eq_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '地震类型',
  `latitude` double(10,5) DEFAULT NULL COMMENT '纬度',
  `longitude` double(10,5) DEFAULT NULL COMMENT '经度',
  `magnitude` double DEFAULT NULL COMMENT '震级',
  `seed_path` varchar(255) DEFAULT NULL COMMENT 'miniSeed路径',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for config
-- ----------------------------
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
  `pwave_before` int(11) NOT NULL COMMENT 'p波到时前n秒',
  `pwave_after` int(11) NOT NULL COMMENT 'p波到时后n秒',
  `storeTime` int(11) DEFAULT '86400',
  `model1` int(11) DEFAULT '1',
  `model2` int(11) DEFAULT '1',
  `model3` int(11) DEFAULT '1',
  `id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
