package com.example.demo.account.dto;

public class AuthDTO {  //used for JWT
    private String token;
    private String username;

    public AuthDTO(){

    }
    public AuthDTO(String token, String username){
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "AuthDTO{" +
                "token='" + token + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
