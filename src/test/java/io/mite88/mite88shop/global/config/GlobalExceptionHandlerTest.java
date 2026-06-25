package io.mite88.mite88shop.global.config;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerTest.TestExceptionController.class) // 이 줄 추가
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 테스트를 위한 더미 컨트롤러
    @RestController
    @RequestMapping("/test-exceptions")
    static class TestExceptionController {
        @GetMapping("/business")
        public void throwBusinessException() {
            throw new BusinessException(ResponseCode.INVALID_PASSWORD);
        }

        @GetMapping("/username-not-found")
        public void throwUsernameNotFoundException() {
            throw new UsernameNotFoundException("User not found for test");
        }

        @GetMapping("/data-integrity-violation")
        public void throwDataIntegrityViolationException() {
            throw new DataIntegrityViolationException("Data integrity violation for test");
        }

        @GetMapping("/general")
        public void throwGeneralException() throws Exception {
            throw new Exception("General exception for test");
        }
    }

    @Test
    @DisplayName("BusinessException 처리 테스트")
    void handleBusinessExceptionTest() throws Exception {
        mockMvc.perform(get("/test-exceptions/business")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized()) // INVALID_PASSWORD의 HttpStatus
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ResponseCode.INVALID_PASSWORD.getCode()))
                .andExpect(jsonPath("$.message").value(ResponseCode.INVALID_PASSWORD.getMessage()));
    }

    @Test
    @DisplayName("UsernameNotFoundException 처리 테스트")
    void handleUsernameNotFoundExceptionTest() throws Exception {
        mockMvc.perform(get("/test-exceptions/username-not-found")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized()) // USER_NOT_FOUND의 HttpStatus
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ResponseCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ResponseCode.USER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("DataIntegrityViolationException 처리 테스트")
    void handleDataIntegrityViolationExceptionTest() throws Exception {
        mockMvc.perform(get("/test-exceptions/data-integrity-violation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict()) // DUPLICATE_MEMBER의 HttpStatus
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ResponseCode.DUPLICATE_MEMBER.getCode()))
                .andExpect(jsonPath("$.message").value(ResponseCode.DUPLICATE_MEMBER.getMessage()));
    }

    @Test
    @DisplayName("General Exception 처리 테스트")
    void handleGeneralExceptionTest() throws Exception {
        mockMvc.perform(get("/test-exceptions/general")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError()) // This should pass now because the custom exception handler is in effect
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ResponseCode.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(ResponseCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
