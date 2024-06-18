package com.sparta.fifteen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.fifteen.config.WebSecurityConfig;
import com.sparta.fifteen.controller.UserController;
import com.sparta.fifteen.dto.UserRegisterRequestDto;
import com.sparta.fifteen.dto.UserRegisterResponseDto;
import com.sparta.fifteen.dto.UserRequestDto;
import com.sparta.fifteen.entity.User;
import com.sparta.fifteen.error.UserAlreadyExistsException;
import com.sparta.fifteen.repository.UserRepository;
import com.sparta.fifteen.security.UserDetailsServiceImpl;
import com.sparta.fifteen.service.AuthenticationService;
import com.sparta.fifteen.service.UserService;
import org.apache.catalina.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.PrintingResultHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = {UserController.class})
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    // 실제 빈을 목 객체로 대체
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private UserRequestDto userRequestDto;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        userRequestDto = new UserRequestDto("asdfg12345", "TestPassword123!");
    }


    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("회원 가입 성공 테스트")
    public void signupTest_success() throws Exception {
        // given
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
        requestDto.setUsername("asdfg12345");
        requestDto.setPassword("TestPassword123!");

        User registeredUser = new User();
        registeredUser.setUsername(requestDto.getUsername());
        registeredUser.setPassword(requestDto.getPassword());

        UserRegisterResponseDto responseDto = new UserRegisterResponseDto(registeredUser);

        // UserService의 registerUser 메서드가 호출될 때 responseDto를 반환하도록 설정
        when(userService.registerUser(any(UserRegisterRequestDto.class))).thenReturn(responseDto);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestDtoJson = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDtoJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print());

    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("회원 가입 실패 테스트 - 이미 존재하는 username")
    public void signupTest_DuplicatedUsername_failed() throws Exception {
        // given
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
        requestDto.setUsername("existingUser");
        requestDto.setPassword("TestPassword123!");

        // 모의 객체 설정: 이미 존재하는 username일 때 예외 발생 시나리오 설정
        when(userService.registerUser(any(UserRegisterRequestDto.class)))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));

        ObjectMapper objectMapper = new ObjectMapper();
        String requestDtoJson = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDtoJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(print());
    }


    @Test
    @DisplayName("로그인 테스트 - 성공")
    public void loginTest_success() throws Exception {
        String requestDtoJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDtoJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("로그인 테스트 - 성공 mockUser")
    @WithMockUser(username = "asdfg12345", password = "TestPassword123!")
    public void loginTest_success_withMockUser() throws Exception {
        String requestDtoJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDtoJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    @DisplayName("로그인 테스트 - 실패")
    public void loginTest_fail() throws Exception {
        // given
        UserRequestDto requestDto = new UserRequestDto("asdfg12345", "TestPassword123!");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestDtoJson = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDtoJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(print());
    }

}

