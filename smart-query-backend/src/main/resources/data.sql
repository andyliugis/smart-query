-- 示例销售数据
INSERT INTO sales_data (product_name, category, sale_amount, sale_quantity, sale_date, region, salesperson) VALUES
('iPhone 16', '手机', 8999.00, 120, '2026-05-01', '华东', '张三'),
('iPhone 16', '手机', 8999.00, 85, '2026-05-05', '华南', '李四'),
('iPhone 16', '手机', 8999.00, 95, '2026-05-12', '华北', '王五'),
('Mate 70', '手机', 6999.00, 150, '2026-05-02', '华东', '张三'),
('Mate 70', '手机', 6999.00, 110, '2026-05-08', '华南', '李四'),
('Mate 70', '手机', 6999.00, 88, '2026-05-20', '西南', '赵六'),
('MacBook Pro', '电脑', 15999.00, 45, '2026-05-03', '华东', '张三'),
('MacBook Pro', '电脑', 15999.00, 30, '2026-05-15', '华北', '王五'),
('ThinkPad X1', '电脑', 9999.00, 60, '2026-05-04', '华南', '李四'),
('ThinkPad X1', '电脑', 9999.00, 40, '2026-05-18', '华东', '张三'),
('AirPods Pro', '配件', 1999.00, 300, '2026-05-01', '华东', '张三'),
('AirPods Pro', '配件', 1999.00, 250, '2026-05-10', '华南', '李四'),
('AirPods Pro', '配件', 1999.00, 180, '2026-05-22', '华北', '王五'),
('iPad Air', '平板', 4999.00, 80, '2026-05-06', '华东', '张三'),
('iPad Air', '平板', 4999.00, 65, '2026-05-14', '西南', '赵六'),
('小米手环', '配件', 299.00, 500, '2026-05-02', '华南', '李四'),
('小米手环', '配件', 299.00, 420, '2026-05-11', '华东', '张三'),
('小米手环', '配件', 299.00, 350, '2026-05-25', '华北', '王五'),
('Galaxy S26', '手机', 7999.00, 70, '2026-05-07', '华东', '张三'),
('Galaxy S26', '手机', 7999.00, 55, '2026-05-19', '华南', '李四'),
('Dell显示器', '配件', 2999.00, 90, '2026-05-09', '华北', '王五'),
('Dell显示器', '配件', 2999.00, 75, '2026-05-23', '华东', '张三'),
('AirPods Pro', '配件', 1999.00, 200, '2026-06-01', '华东', '张三'),
('iPhone 16', '手机', 8999.00, 60, '2026-06-02', '华南', '李四'),
('Mate 70', '手机', 6999.00, 95, '2026-06-03', '华北', '王五');

-- ===== 语义层预置数据 =====

-- 表元数据 (id=1)
INSERT INTO table_metadata (id, datasource_id, table_name, display_name, description, sync_time)
VALUES (1, 1, 'sales_data', '销售数据表', '记录各产品的销售明细数据，包含产品、金额、数量、日期、区域等信息', CURRENT_TIMESTAMP);

-- 字段元数据
INSERT INTO column_metadata (table_id, column_name, display_name, data_type, role, description, enum_values) VALUES
(1, 'id', '主键ID', 'BIGINT', 'none', '自增主键', NULL),
(1, 'product_name', '产品名称', 'VARCHAR(100)', 'dimension', '销售的具体产品名', 'iPhone 16,Mate 70,MacBook Pro,ThinkPad X1,AirPods Pro,iPad Air,小米手环,Galaxy S26,Dell显示器'),
(1, 'category', '产品类别', 'VARCHAR(50)', 'dimension', '产品所属类别', '手机,电脑,平板,配件'),
(1, 'sale_amount', '销售单价', 'DECIMAL(12,2)', 'metric', '单笔销售的产品单价', NULL),
(1, 'sale_quantity', '销售数量', 'INT', 'metric', '单笔销售的产品数量', NULL),
(1, 'sale_date', '销售日期', 'DATE', 'dimension', '销售发生的日期', NULL),
(1, 'region', '销售区域', 'VARCHAR(50)', 'dimension', '销售发生的地理区域', '华东,华南,华北,西南'),
(1, 'salesperson', '销售人员', 'VARCHAR(50)', 'dimension', '负责该笔销售的人员', '张三,李四,王五,赵六');

-- 指标定义
INSERT INTO metric_definition (table_id, name, description, expression, agg_function, unit) VALUES
(1, '销售额', '产品销售的总金额，等于单价乘以数量', 'sale_amount * sale_quantity', 'SUM', '元'),
(1, '销售数量', '产品的总销售件数', 'sale_quantity', 'SUM', '件'),
(1, '平均单价', '产品的平均销售单价', 'sale_amount', 'AVG', '元'),
(1, '最高单价', '产品的最高销售单价', 'sale_amount', 'MAX', '元'),
(1, '销售笔数', '销售记录的总条数', '*', 'COUNT', '笔');

-- 维度定义
INSERT INTO dimension_definition (table_id, name, description, column_name, enum_values) VALUES
(1, '产品名称', '按具体产品分组', 'product_name', 'iPhone 16,Mate 70,MacBook Pro,ThinkPad X1,AirPods Pro,iPad Air,小米手环,Galaxy S26,Dell显示器'),
(1, '产品类别', '按产品类型分组', 'category', '手机,电脑,平板,配件'),
(1, '销售区域', '按地理区域分组', 'region', '华东,华南,华北,西南'),
(1, '销售人员', '按销售人员分组', 'salesperson', '张三,李四,王五,赵六'),
(1, '销售日期', '按日期分组，支持按月/周/日聚合', 'sale_date', NULL);

-- ===== 初始用户数据 =====
-- 密码是 "admin123" (明文用于测试，实际项目中需要加密!)
INSERT INTO sys_user (username, password, email, nickname, status) VALUES
('admin', 'admin123', 'admin@example.com', '管理员', 'ACTIVE'),
('demo', 'admin123', 'demo@example.com', '演示用户', 'ACTIVE');

-- ===== 默认标签 =====
INSERT INTO tag_definition (name, color, description) VALUES
('销售数据', '#ef4444', '销售相关的数据表'),
('用户数据', '#3b82f6', '用户相关的数据表'),
('订单数据', '#8b5cf6', '订单相关的数据表'),
('产品数据', '#10b981', '产品相关的数据表'),
('财务数据', '#f59e0b', '财务相关的数据表'),
('日志数据', '#6b7280', '日志和审计相关的数据表');

-- 为销售数据表添加标签
INSERT INTO table_tag_relation (table_id, tag_id) VALUES
(1, 1), -- sales_data -> 销售数据
(1, 4); -- sales_data -> 产品数据
