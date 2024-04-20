package com.feng.messagequeue.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseEvent {
    private String id;
}
