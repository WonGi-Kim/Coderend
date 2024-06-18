package com.sparta.fifteen;

import com.sparta.fifteen.dto.UserRegisterRequestDto;
import com.sparta.fifteen.dto.UserRegisterResponseDto;
import com.sparta.fifteen.entity.User;
import com.sparta.fifteen.entity.UserStatusEnum;
import com.sparta.fifteen.error.PasswordMismatchException;
import com.sparta.fifteen.error.UserAlreadyExistsException;
import com.sparta.fifteen.error.UserWithdrawnException;
import com.sparta.fifteen.repository.UserRepository;
import com.sparta.fifteen.service.EmailVerificationService;
import com.sparta.fifteen.service.LogoutAccessTokenService;
import com.sparta.fifteen.service.RefreshTokenService;
import com.sparta.fifteen.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;  // 사용자 리포지토리를 모킹합니다.

    @Mock
    private PasswordEncoder passwordEncoder;  // 패스워드 인코더를 모킹합니다.

    @Mock
    private RefreshTokenService refreshTokenService;  // 리프레시 토큰 서비스를 모킹합니다.

    @Mock
    private EmailVerificationService emailVerificationService;  // 이메일 인증 서비스를 모킹합니다.

    @Mock
    private LogoutAccessTokenService logoutAccessTokenService;  // 로그아웃 액세스 토큰 서비스를 모킹합니다.

    @InjectMocks
    private UserService userService;  // 테스트할 UserService를 주입합니다.

    private User existingUser;
    private UserRegisterRequestDto requestDto;

    @BeforeEach
    void setUp() {
        // 초기화 작업 : 존재하는 사용자 기존 값 설정
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("asdfg12345");  // 기존 사용자의 아이디 설정
        existingUser.setPassword("hashedPassword");  // 기존 사용자의 암호화된 비밀번호 설정
        existingUser.setStatusCode(String.valueOf(UserStatusEnum.NORMAL.getStatus()));  // 기존 사용자의 상태 코드 설정

        // 새로운 사용자 등록 요청 DTO 설정
        requestDto = new UserRegisterRequestDto();
        requestDto.setUsername("qwert12345");  // 새 사용자의 아이디 설정
        requestDto.setPassword("validPassword123");  // 새 사용자의 유효한 비밀번호 설정
    }

    @Test
    @DisplayName("새로운 유저 생성 성공")
    void registerUser_NewUser_Success() {
        // Given
        when(userRepository.existsByUsername(requestDto.getUsername())).thenReturn(false);  // 사용자가 존재하지 않는 것으로 설정
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("hashedPassword");  // 패스워드 인코딩 결과 설정
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(2L); // 사용자 저장 및 ID 설정 시뮬레이션
            return userToSave;
        });

        // When
        UserRegisterResponseDto responseDto = userService.registerUser(requestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(requestDto.getUsername(), responseDto.getUsername());
    }

    @Test
    @DisplayName("새로운 유저 생성 실패 - 이미 존재하는 유저네임")
    void registerUser_ExistingUser_fail() {
        // Given
        // 사용자가 이미 존재하는 것으로 설정
        when(userRepository.existsByUsername(existingUser.getUsername())).thenReturn(true);
        // 기존 사용자 반환 설정
        when(userRepository.findByUsername(existingUser.getUsername())).thenReturn(Optional.of(existingUser));

        // When / Then
        UserRegisterRequestDto existingUserRequest = new UserRegisterRequestDto();
        existingUserRequest.setUsername(existingUser.getUsername());  // 기존 사용자의 아이디 설정
        existingUserRequest.setPassword("validPassword123");  // 새 사용자의 유효한 비밀번호 설정
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(existingUserRequest));
    }

    @Test
    @DisplayName("새로운 유저 생성 실패 - 짧은 비밀번호")
    void registerUser_ShortPassword_fail() {
        // Given
        requestDto.setPassword("short");  // 짧은 비밀번호 설정

        // When / Then
        assertThrows(PasswordMismatchException.class, () -> userService.registerUser(requestDto));
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdrawUser_ValidCredentials_success() {
        // Given
        String username = "asdfg12345";  // 사용자 아이디 설정
        String password = "TestPassword1234!";  // 사용자 비밀번호 설정
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(password, existingUser.getPassword())).thenReturn(true);

        // When
        assertDoesNotThrow(() -> userService.withdrawUser(username, password));  // 예외가 발생하지 않는지 확인

        // Then
        assertEquals(String.valueOf(UserStatusEnum.WITHDRAWN.getStatus()), existingUser.getStatusCode());  // 사용자 상태 코드가 탈퇴로 변경되었는지 확인
        verify(refreshTokenService, times(1)).deleteByUser(existingUser);  // 리프레시 토큰 서비스가 한 번 호출되었는지 확인
        verify(logoutAccessTokenService, times(1)).deleteByUsername(username);  // 로그아웃 액세스 토큰 서비스가 한 번 호출되었는지 확인
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 잘못된 비밀번호")
    void withdrawUser_WrongPassword_ExceptionThrown() {
        // Given
        String username = "asdfg12345";  // 사용자 아이디 설정
        String password = "InvalidPassword1234";  // 잘못된 비밀번호 설정
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));  // 사용자가 존재하는 것으로 설정
        when(passwordEncoder.matches(password, existingUser.getPassword())).thenReturn(false);  // 비밀번호 불일치 설정

        // When / Then
        assertThrows(PasswordMismatchException.class, () -> userService.withdrawUser(username, password));  // 예외가 발생하는지 확인
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 이미 탈퇴된 회원")
    void withdrawUser_UserAlreadyWithdrawn_ExceptionThrown() {
        // Given
        String username = "asdfg12345";  // 사용자 아이디 설정
        String password = "TestPassword1234!";  // 사용자 비밀번호 설정
        existingUser.setStatusCode(String.valueOf(UserStatusEnum.WITHDRAWN.getStatus()));  // 사용자 상태 코드를 탈퇴로 설정
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));  // 사용자가 존재하는 것으로 설정
        when(passwordEncoder.matches(password, existingUser.getPassword())).thenReturn(true);  // 비밀번호 일치 설정

        // When / Then
        assertThrows(UserWithdrawnException.class, () -> userService.withdrawUser(username, password));  // 예외가 발생하는지 확인
    }
}
