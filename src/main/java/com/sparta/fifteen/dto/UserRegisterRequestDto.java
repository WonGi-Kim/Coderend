package com.sparta.fifteen.dto;

import jakarta.persistence.Basic;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
public class UserRegisterRequestDto {
    @Size(min = 10, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9]*$")
    private String username;

    @Size(min = 10)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{10,}$")
    private String password;

    private String name; // 이름

    @Email
    private String email;

    private String oneLine;

    private String statusCode;
    private String refreshToken;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Timestamp statusChangedTime;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Timestamp createdOn;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Timestamp modifiedOn;
}
