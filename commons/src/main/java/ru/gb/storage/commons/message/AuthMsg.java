package ru.gb.storage.commons.message;

public class AuthMsg extends Msg {

    private String login;
    private String password;

    public AuthMsg(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public AuthMsg() {
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "AuthMsg{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
