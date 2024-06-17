package com.sparta.fifteen;

import com.sparta.fifteen.dto.UserRegisterRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;


@Validated
@DisplayName("회원 가입 Dto 생성")
public class UserRegisterRequestDtoTest {

    private Validator validator;

    public UserRegisterRequestDtoTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {

    }

    @DisplayName("회원 가입 성공")
    @Test
    public void userRegisterRequestDtoTest_success() {
        // given
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
        requestDto.setUsername("asdfg12345");
        requestDto.setPassword("TestPassword123!");

        // when
        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validator.validate(requestDto);

        // then
        assertTrue(violations.isEmpty(), "Username 혹은 Password 중 하나 이상이 정규 표현식을 만족하지 않음");
    }

    @DisplayName("잘못된 이메일 형식")
    @Test
    public void userRegisterRequestDtoTest_email_fail() {
        // given
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
        requestDto.setEmail("test@test.com");

        // when
        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validator.validate(requestDto);

        // then
        assertTrue(violations.isEmpty(), "잘못된 이메일 형식");
    }
}
