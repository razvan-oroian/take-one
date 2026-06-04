package service.dtos;

import java.time.LocalDate;

public class UserDto {
    private String username;
    private String email;
    private String password;
    private LocalDate joinedDate;

    public UserDto(String username, String email, String password, LocalDate joinedDate) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.joinedDate = joinedDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }
}
