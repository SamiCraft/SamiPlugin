package com.samifying.plugin.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BackendData {
    private String id;
    private String name;
    private String nickname;
    private String avatar;
    private boolean supporter;
    private boolean moderator;
}
