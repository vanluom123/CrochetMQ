package com.feng.messagequeue.event;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseEvent {
    private String id;
}
