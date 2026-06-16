-- 销售数据表
CREATE TABLE IF NOT EXISTS sales_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    category VARCHAR(50) NOT NULL COMMENT '产品类别',
    sale_amount DECIMAL(12, 2) NOT NULL COMMENT '销售金额',
    sale_quantity INT NOT NULL COMMENT '销售数量',
    sale_date DATE NOT NULL COMMENT '销售日期',
    region VARCHAR(50) NOT NULL COMMENT '销售区域',
    salesperson VARCHAR(50) NOT NULL COMMENT '销售人员'
);

-- ===== 元数据管理表 =====

-- 1. 数据源配置
CREATE TABLE IF NOT EXISTS datasource_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    db_type VARCHAR(30) NOT NULL COMMENT '数据库类型: H2/MYSQL/CLICKHOUSE',
    jdbc_url VARCHAR(500) NOT NULL COMMENT 'JDBC连接地址',
    username VARCHAR(100) COMMENT '用户名',
    password VARCHAR(200) COMMENT '密码',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active/inactive',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 表元数据
CREATE TABLE IF NOT EXISTS table_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    datasource_id BIGINT NOT NULL COMMENT '关联数据源',
    table_name VARCHAR(200) NOT NULL COMMENT '物理表名',
    display_name VARCHAR(200) COMMENT '业务名称',
    description VARCHAR(500) COMMENT '业务描述',
    sync_time TIMESTAMP COMMENT '最后同步时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 字段元数据
CREATE TABLE IF NOT EXISTS column_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL COMMENT '关联表',
    column_name VARCHAR(200) NOT NULL COMMENT '物理列名',
    display_name VARCHAR(200) COMMENT '业务名称',
    data_type VARCHAR(100) COMMENT '数据类型',
    role VARCHAR(20) DEFAULT 'none' COMMENT '字段角色: dimension/metric/none',
    description VARCHAR(500) COMMENT '业务描述',
    enum_values VARCHAR(1000) COMMENT '枚举值,逗号分隔',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 指标定义
CREATE TABLE IF NOT EXISTS metric_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL COMMENT '所属表',
    name VARCHAR(100) NOT NULL COMMENT '指标名称',
    description VARCHAR(500) COMMENT '业务定义',
    expression VARCHAR(500) NOT NULL COMMENT 'SQL表达式',
    agg_function VARCHAR(20) NOT NULL COMMENT '聚合方式: SUM/AVG/COUNT/MAX/MIN',
    unit VARCHAR(50) COMMENT '单位',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. 维度定义
CREATE TABLE IF NOT EXISTS dimension_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL COMMENT '所属表',
    name VARCHAR(100) NOT NULL COMMENT '维度名称',
    description VARCHAR(500) COMMENT '业务定义',
    column_name VARCHAR(200) NOT NULL COMMENT '关联字段',
    enum_values VARCHAR(1000) COMMENT '枚举值,逗号分隔',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. 标签定义
CREATE TABLE IF NOT EXISTS tag_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    color VARCHAR(20) DEFAULT '#3b82f6' COMMENT '标签颜色',
    description VARCHAR(200) COMMENT '标签描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. 表标签关联
CREATE TABLE IF NOT EXISTS table_tag_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL COMMENT '表ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_table_tag (table_id, tag_id)
);

-- 8. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    email VARCHAR(100) COMMENT '邮箱',
    nickname VARCHAR(50) COMMENT '昵称',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. 查询审计日志表
CREATE TABLE IF NOT EXISTS query_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    question TEXT NOT NULL COMMENT '用户问题',
    generated_sql TEXT COMMENT '生成的SQL',
    execution_time_ms BIGINT COMMENT '执行耗时(毫秒)',
    result_count INT COMMENT '结果条数',
    success BOOLEAN DEFAULT TRUE COMMENT '是否成功',
    error_message TEXT COMMENT '错误信息',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT 'User Agent',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. 数据权限表
CREATE TABLE IF NOT EXISTS data_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    table_name VARCHAR(200) NOT NULL COMMENT '表名',
    column_name VARCHAR(200) COMMENT '列名(为空表示整表)',
    permission_type VARCHAR(20) DEFAULT 'READ' COMMENT '权限类型: READ/WRITE',
    condition_expression TEXT COMMENT '行级过滤条件(如: region = ''北京'')',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. 对话会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) COMMENT '会话标题',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/ARCHIVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. 对话消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色: user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    generated_sql TEXT COMMENT '生成的SQL(仅assistant消息)',
    result_data TEXT COMMENT '查询结果JSON(仅assistant消息)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. 查询反馈表
CREATE TABLE IF NOT EXISTS query_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT COMMENT '会话ID',
    message_id BIGINT COMMENT '消息ID',
    user_id BIGINT COMMENT '用户ID',
    feedback_type VARCHAR(20) NOT NULL COMMENT '反馈类型: like/dislike',
    content TEXT COMMENT '反馈内容(点踩时可填原因)',
    question TEXT COMMENT '原始问题',
    generated_sql TEXT COMMENT '生成的SQL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
