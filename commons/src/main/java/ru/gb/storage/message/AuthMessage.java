package ru.gb.storage.message;

public class AuthMessage extends Message{

    private String login;
    private String pass;

    public String getLogin () { return login;}
    public void setLogin(String nick) { this.login=login; }
    public String getPass () { return pass;}
    public void setPass(String pass) {this.pass= this.pass; }

    @Override
    public String toString () {
        return "AuthMessage {"+
                "login="+login + '\'' +
                "pass="+pass+ '\'' +
                '}';
    }
}
