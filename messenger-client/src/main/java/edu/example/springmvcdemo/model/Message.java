package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Entity
@Table(name = "messages")
@Data
public class Message {

    @EmbeddedId
    private MessageId messageId;

    @ManyToOne
    @MapsId("roomId")
    @JoinColumn(nullable = false, name = "room_id")
    private Room room;

    @Column(nullable = false, name = "timestamp")
    private Timestamp timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "data_type")
    private DataType dataType;

    @Lob
    @Column(nullable = false, name = "data")
    private byte[] data;
}