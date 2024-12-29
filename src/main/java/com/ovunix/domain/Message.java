package com.ovunix.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "T_MESSAGE")
public class Message extends AbstractEntity{

    @Id
    @Column(name = "MESSAGE_CD")
    private String messageCd;

    @ElementCollection
    @CollectionTable(name = "T_MESSAGE_LNG", joinColumns = @JoinColumn(name = "MESSAGE_CD"))
    private List<MessageDetails> details;

    @Override
    public void setId(Serializable id) {
      this.messageCd= (String) id;
    }

    @Override
    public Serializable getId() {
        return this.messageCd;
    }
}
