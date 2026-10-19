package com.iflytek.rpa.resource.file.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.iflytek.rpa.resource.common.auth.RpaAuthClient;
import com.iflytek.rpa.resource.common.exp.ServiceException;
import com.iflytek.rpa.resource.common.response.ErrorCodeEnum;
import com.iflytek.rpa.resource.file.config.S3Config;
import com.iflytek.rpa.resource.file.dao.FileMapper;
import com.iflytek.rpa.resource.file.entity.File;
import com.iflytek.rpa.resource.file.utils.IdWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class FileServiceImplTest {

    private FileServiceImpl fileService;
    private FileMapper fileMapper;
    private S3Config s3Config;
    private RpaAuthClient rpaAuthClient;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl();
        fileMapper = Mockito.mock(FileMapper.class);
        s3Config = Mockito.mock(S3Config.class);
        rpaAuthClient = Mockito.mock(RpaAuthClient.class);
        ReflectionTestUtils.setField(fileService, "baseMapper", fileMapper);
        ReflectionTestUtils.setField(fileService, "s3Config", s3Config);
        ReflectionTestUtils.setField(fileService, "rpaAuthClient", rpaAuthClient);
        ReflectionTestUtils.setField(fileService, "idWorker", Mockito.mock(IdWorker.class));
    }

    @Test
    void downloadOfAnotherTenantsFileIsTreatedAsNotFound() {
        when(rpaAuthClient.getCurrentTenantId()).thenReturn("tenant-b");
        // 文件属于 tenant-a，按 tenant-b 查询不到
        when(fileMapper.getFile("1847000000000000001", "tenant-b")).thenReturn(null);

        assertThatThrownBy(() -> fileService.downloadFile("1847000000000000001"))
                .isInstanceOf(ServiceException.class)
                .hasMessage(ErrorCodeEnum.E_SQL_EMPTY.getCode());
        verify(fileMapper).getFile("1847000000000000001", "tenant-b");
        verifyNoInteractions(s3Config);
    }

    @Test
    void downloadRequiresLogin() {
        when(rpaAuthClient.getCurrentTenantId()).thenThrow(new ServiceException(ErrorCodeEnum.E_NOT_LOGIN));

        assertThatThrownBy(() -> fileService.downloadFile("1847000000000000001"))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeEnum.E_NOT_LOGIN.getCode());
        verifyNoInteractions(fileMapper, s3Config);
    }

    @Test
    void uploadRequiresLoginBeforeWritingToS3() {
        when(rpaAuthClient.getCurrentTenantId()).thenThrow(new ServiceException(ErrorCodeEnum.E_NOT_LOGIN));
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", new byte[] {1});

        assertThatThrownBy(() -> fileService.uploadFile(file))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeEnum.E_NOT_LOGIN.getCode());
        verifyNoInteractions(fileMapper, s3Config);
    }

    @Test
    void savedFileRecordsTenantAndCreator() {
        ReflectionTestUtils.invokeMethod(
                fileService,
                "saveFileInfo",
                "1847000000000000001",
                "rpa/2026-10-19/a.txt",
                "a.txt",
                "tenant-a",
                "user-a");

        ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
        verify(fileMapper).insert(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo("tenant-a");
        assertThat(captor.getValue().getCreatorId()).isEqualTo("user-a");
    }
}
