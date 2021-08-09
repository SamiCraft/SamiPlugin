package com.samifying.plugin.atributes;

public class BackendData {
    private String id;
    private String name;
    private String nickname;
    private String avatar;
    private boolean supporter;
    private boolean moderator;

    public BackendData() {
    }

    public BackendData(String id, String name, String nickname, String avatar, boolean supporter, boolean moderator) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.avatar = avatar;
        this.supporter = supporter;
        this.moderator = moderator;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isSupporter() {
        return supporter;
    }

    public boolean isModerator() {
        return moderator;
    }
}
