package com.iflytek.rpa.resource.file.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iflytek.rpa.resource.file.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文件表 Mapper 接口
 *
 * @author system
 * @since 2024-01-01
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {

    /**
     * 按文件ID查询当前租户的文件，不属于该租户的文件视为不存在
     */
    @Select("select * from file where deleted = 0 and file_id = #{fileId} and tenant_id = #{tenantId}")
    File getFile(@Param("fileId") String fileId, @Param("tenantId") String tenantId);
}
