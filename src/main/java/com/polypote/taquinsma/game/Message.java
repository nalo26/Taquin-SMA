package com.polypote.taquinsma.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Message {

    private Agent sender;

    private Agent receiver;

    private MessageType type;

    public void send() {
        this.receiver.addMessage(this);
    }
}
