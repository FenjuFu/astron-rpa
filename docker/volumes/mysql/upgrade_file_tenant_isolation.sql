-- =====================================================================
-- CVE-2026-110764：resource-service /file/download 跨租户越权下载修复
--
-- 新版 resource-service 仅允许下载当前租户上传的文件，因此需要：
--   1. 为 file 表增加 tenant_id / creator_id 字段；
--   2. 根据引用文件的业务表回填历史文件的归属。
--
-- 脚本可重复执行：docker compose 每次启动 mysql 时会自动执行。
-- 每条历史文件只回填一次：无法确定归属的文件 tenant_id 置为 ''，之后不再推断，
-- 避免有人事后在业务表中伪造引用来"认领"这些文件。
-- 未使用本仓库 docker-compose.yml 的部署，请备份数据库后手动执行一次：
--   mysql -u<用户名> -p <数据库名> < upgrade_file_tenant_isolation.sql
-- =====================================================================

-- 为 file 表增加归属字段（已存在则跳过，例如已由 atlas 按 schema.hcl 创建）
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

-- 回填历史文件（tenant_id IS NULL）的归属：
-- 仅采用未删除的引用，且只有一个租户引用该文件时才归属给它；
-- 被多个租户引用的文件无法确定归属，不分配给任何租户。
-- shared_file.file_id 为 BIGINT：直接与字符串比较会按 DOUBLE 比较，雪花ID丢失精度而错配租户；
-- CONCAT 转为可强制转换的字符串，既能精确比较，又不会产生排序规则冲突。
UPDATE `file` f
    JOIN (
        SELECT ref.file_id, MIN(ref.tenant_id) AS tenant_id, MIN(ref.creator_id) AS creator_id
        FROM (
            SELECT CONCAT(s.`file_id`) AS file_id, s.`tenant_id`, s.`creator_id`
            FROM `shared_file` s
            WHERE s.`deleted` = 0
            UNION ALL
            SELECT rv.`video_id`, rv.`tenant_id`, rv.`creator_id`
            FROM `robot_version` rv
            WHERE rv.`deleted` = 0
            UNION ALL
            SELECT rv.`appendix_id`, rv.`tenant_id`, rv.`creator_id`
            FROM `robot_version` rv
            WHERE rv.`deleted` = 0
            UNION ALL
            -- 元素截图（c_element 无租户字段，通过 robot_design 关联租户）
            SELECT e.`image_id`, rd.`tenant_id`, e.`creator_id`
            FROM `c_element` e
                JOIN `robot_design` rd ON rd.`robot_id` = e.`robot_id`
            WHERE e.`deleted` = 0 AND rd.`deleted` = 0
            UNION ALL
            SELECT e.`parent_image_id`, rd.`tenant_id`, e.`creator_id`
            FROM `c_element` e
                JOIN `robot_design` rd ON rd.`robot_id` = e.`robot_id`
            WHERE e.`deleted` = 0 AND rd.`deleted` = 0
        ) ref
        WHERE ref.file_id IS NOT NULL AND ref.file_id <> ''
          AND ref.tenant_id IS NOT NULL AND ref.tenant_id <> ''
        GROUP BY ref.file_id
        HAVING COUNT(DISTINCT ref.tenant_id) = 1
    ) owner ON f.`file_id` = owner.file_id
SET f.`tenant_id` = owner.tenant_id, f.`creator_id` = owner.creator_id
WHERE f.`tenant_id` IS NULL;

-- 其余无法确定归属的历史文件（如富文本中插入的图片、反馈截图）标记为 ''，升级后无法下载，且不再回填。
-- 单租户部署确认后，可手动将其归属到该租户：
--   UPDATE `file` SET `tenant_id` = '<租户ID>' WHERE `tenant_id` = '';
UPDATE `file` SET `tenant_id` = '' WHERE `tenant_id` IS NULL;

SELECT COUNT(*) AS unowned_files FROM `file` WHERE `tenant_id` = '' AND `deleted` = 0;
