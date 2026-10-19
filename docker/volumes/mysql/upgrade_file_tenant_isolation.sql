-- =====================================================================
-- CVE-2026-110764：resource-service /file/download 跨租户越权下载修复
--
-- 新版 resource-service 仅允许下载当前租户上传的文件，因此需要：
--   1. 为 file 表增加 tenant_id / creator_id 字段；
--   2. 根据引用文件的业务表回填历史文件的归属。
--
-- 脚本可重复执行：docker compose 每次启动 mysql 时会自动执行。
-- 未使用本仓库 docker-compose.yml 的部署，请备份数据库后手动执行一次：
--   mysql -u<用户名> -p <数据库名> < upgrade_file_tenant_isolation.sql
-- =====================================================================

-- 为 file 表增加归属字段（已存在则跳过）
SET @file_owner_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'file' AND COLUMN_NAME = 'tenant_id') = 0,
    'ALTER TABLE `file`
         ADD COLUMN `tenant_id` char(36) DEFAULT NULL COMMENT ''租户id'',
         ADD COLUMN `creator_id` char(36) DEFAULT NULL COMMENT ''上传者id'',
         ADD KEY `idx_file_id_tenant_id` (`file_id`, `tenant_id`)',
    'DO 0');
PREPARE file_owner_stmt FROM @file_owner_ddl;
EXECUTE file_owner_stmt;
DEALLOCATE PREPARE file_owner_stmt;

-- 共享文件
-- shared_file.file_id 为 BIGINT：直接与字符串比较会按 DOUBLE 比较，雪花ID丢失精度而错配租户；
-- CONCAT 转为可强制转换的字符串，既能精确比较、避免排序规则冲突，又能使用 file_id 索引
UPDATE `file` f
    JOIN `shared_file` s ON f.`file_id` = CONCAT(s.`file_id`)
SET f.`tenant_id` = s.`tenant_id`, f.`creator_id` = s.`creator_id`
WHERE f.`tenant_id` IS NULL AND s.`tenant_id` IS NOT NULL;

-- 机器人版本的视频、附件
UPDATE `file` f
    JOIN `robot_version` rv ON f.`file_id` = rv.`video_id`
SET f.`tenant_id` = rv.`tenant_id`, f.`creator_id` = rv.`creator_id`
WHERE f.`tenant_id` IS NULL AND rv.`tenant_id` IS NOT NULL;

UPDATE `file` f
    JOIN `robot_version` rv ON f.`file_id` = rv.`appendix_id`
SET f.`tenant_id` = rv.`tenant_id`, f.`creator_id` = rv.`creator_id`
WHERE f.`tenant_id` IS NULL AND rv.`tenant_id` IS NOT NULL;

-- 元素截图（c_element 无租户字段，通过 robot_design 关联租户）
UPDATE `file` f
    JOIN `c_element` e ON f.`file_id` = e.`image_id`
    JOIN `robot_design` rd ON rd.`robot_id` = e.`robot_id`
SET f.`tenant_id` = rd.`tenant_id`, f.`creator_id` = e.`creator_id`
WHERE f.`tenant_id` IS NULL AND rd.`tenant_id` IS NOT NULL;

UPDATE `file` f
    JOIN `c_element` e ON f.`file_id` = e.`parent_image_id`
    JOIN `robot_design` rd ON rd.`robot_id` = e.`robot_id`
SET f.`tenant_id` = rd.`tenant_id`, f.`creator_id` = e.`creator_id`
WHERE f.`tenant_id` IS NULL AND rd.`tenant_id` IS NOT NULL;

-- 仍未归属的历史文件（如富文本中插入的图片、反馈截图）升级后将无法下载。
-- 单租户部署可将其统一归属到该租户：
--   UPDATE `file` SET `tenant_id` = '<租户ID>' WHERE `tenant_id` IS NULL;
SELECT COUNT(*) AS unowned_files FROM `file` WHERE `tenant_id` IS NULL AND `deleted` = 0;
