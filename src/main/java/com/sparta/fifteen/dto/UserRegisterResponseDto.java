package com.sparta.fifteen.dto;

import com.sparta.fifteen.entity.RefreshToken;
import com.sparta.fifteen.entity.User;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
public class UserRegisterResponseDto {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String oneLine;
    private RefreshToken userRefreshToken;
    private String statusCode;
    private Timestamp createdOn;

    public UserRegisterResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
        this.oneLine = user.getOneLine();
        this.userRefreshToken = user.getRefreshToken();
        this.statusCode = user.getStatusCode();
        this.createdOn = user.getCreatedOn();
    }
}
