CREATE DATABASE IF NOT EXISTS ai_rag DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_rag;

-- 聊天会话表
CREATE TABLE IF NOT EXISTS ai_chat_session (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    session_name VARCHAR(200) NOT NULL DEFAULT '新会话' COMMENT '会话名称',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天会话';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS ai_chat_message (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    session_id  BIGINT   NOT NULL COMMENT '会话ID',
    role        VARCHAR(20) NOT NULL COMMENT '角色: user/assistant',
    content     LONGTEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息';

-- 知识库文档表
CREATE TABLE IF NOT EXISTS knowledge_doc (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title       VARCHAR(200) NOT NULL COMMENT '标题',
    content     LONGTEXT     NOT NULL COMMENT '内容',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档';

-- 示例知识库数据
INSERT INTO knowledge_doc (title, content) VALUES
('奖学金申请条件', '国家奖学金申请条件：1. 全日制在校本科生；2. 学习成绩排名在专业前10%；3. 无违纪记录；4. 综合测评成绩优异。'),
('奖学金申请时间', '国家奖学金申请时间为每年9月1日至9月30日，学院审核后于10月15日前上报学校。');
