package ru.gb.storage.message;

public class AuthMessage extends Message{

    private String login;
    private String pass;

    public String getLogin () { return login;}
    public void setLogin(String login) { this.login=login; }
    public String getPass () { return pass; }
    public void setPass(String pass1) { this.pass= pass1; }

    @Override
    public String toString () {
        return "AuthMessage {"+
                "login="+login + '\'' +
                "pass="+pass+ '\'' +
                '}';
    }
}
