package com.samifying.plugin.atributes;

public class BackendData {
    private String id;
    private String name;
    private String avatar;
    private boolean supporter;
    private boolean moderator;

    public BackendData() {
    }

    public BackendData(String id, String name, String avatar, boolean supporter, boolean moderator) {
        this.id = id;
        this.name = name;
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
