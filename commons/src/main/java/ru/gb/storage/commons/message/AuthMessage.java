package ru.gb.storage.commons.message;

public class AuthMessage extends Message {

    private String login;
    private int password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int getPassword() {
        return password;
    }

    public void setPassword(int password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "AuthMessage{" +
                "login='" + login + '\'' +
                ", password=" + password +
                '}';
    }
}
