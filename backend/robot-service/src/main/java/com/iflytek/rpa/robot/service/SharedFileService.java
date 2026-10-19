package com.iflytek.rpa.robot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iflytek.rpa.robot.entity.dto.SharedFileDto;
import com.iflytek.rpa.robot.entity.dto.SharedFilePageDto;
import com.iflytek.rpa.robot.entity.vo.SharedFilePageVo;
import com.iflytek.rpa.utils.exception.NoLoginException;
import com.iflytek.rpa.utils.response.AppResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 共享文件服务接口
 *
 * @author yfchen40
 * @since 2025-07-21
 */
public interface SharedFileService {
    AppResponse<IPage<SharedFilePageVo>> getSharedFilePageList(SharedFilePageDto queryDto);

    AppResponse<?> addSharedFileInfo(HttpServletRequest request, SharedFileDto dto) throws NoLoginException;
}
