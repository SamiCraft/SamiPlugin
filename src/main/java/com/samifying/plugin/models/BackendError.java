package com.samifying.plugin.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BackendError {
    private String name;
    private String message;
    private String path;
    private Long timestamp;
}
