package com.iflytek.rpa.robot.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iflytek.rpa.robot.entity.dto.SharedFileDto;
import com.iflytek.rpa.robot.service.SharedFileService;
import com.iflytek.rpa.utils.response.AppResponse;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RobotSharedFileControllerTest {

    private MockMvc mockMvc;
    private SharedFileService sharedFileService;

    @BeforeEach
    void setUp() {
        sharedFileService = mock(SharedFileService.class);
        RobotSharedFileController controller = new RobotSharedFileController();
        ReflectionTestUtils.setField(controller, "sharedFileService", sharedFileService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldAddSharedFileInfoFromEngineUploadPayload() throws Exception {
        doReturn(AppResponse.success("新增成功")).when(sharedFileService).addSharedFileInfo(any(), any());

        // astronverse-enterprise 的 upload_to_sharefolder 在 share-file-upload 成功后发送的请求体
        String body = "{\"fileId\":\"file-123\",\"fileType\":3,\"fileName\":\"test.txt\",\"tags\":[]}";
        mockMvc.perform(post("/robot-shared-file/addSharedFileInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data").value("新增成功"));

        ArgumentCaptor<SharedFileDto> dtoCaptor = ArgumentCaptor.forClass(SharedFileDto.class);
        verify(sharedFileService).addSharedFileInfo(any(HttpServletRequest.class), dtoCaptor.capture());
        SharedFileDto dto = dtoCaptor.getValue();
        assertEquals("file-123", dto.getFileId());
        assertEquals(Integer.valueOf(3), dto.getFileType());
        assertEquals("test.txt", dto.getFileName());
        assertEquals(Collections.emptyList(), dto.getTags());
    }
}
