package com.feng.messagequeue.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TestEvent extends BaseEvent {
    private String name;
    private int age;

    @Override
    public TestEvent setId(String id) {
        super.setId(id);
        return this;
    }
}
