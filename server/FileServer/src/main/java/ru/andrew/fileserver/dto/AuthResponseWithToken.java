package ru.andrew.fileserver.dto;

import lombok.Setter;

@Setter
public class AuthResponseWithToken extends AuthResponse {
    private String token;

    public AuthResponseWithToken(String message, String token) {
        super(message);
        this.token = token;
    }
}
