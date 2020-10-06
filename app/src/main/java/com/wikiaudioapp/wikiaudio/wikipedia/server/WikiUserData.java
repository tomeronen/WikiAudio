package com.wikiaudioapp.wikiaudio.wikipedia.server;

public class WikiUserData {
    String userName;
    String password;
    boolean bot;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isBot() {
        return bot;
    }

    public void setBot(boolean bot) {
        this.bot = bot;
    }

    public WikiUserData(String userName, String password, boolean bot) {
        this.userName = userName;
        this.password = password;
        this.bot = bot;
    }
}
