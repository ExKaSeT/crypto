package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "rooms")
@Data
public class Room {

    @Id
    @Column(nullable = false, name = "room_id")
    private Long roomId;

    @Column(nullable = false, name = "participant_username")
    private String participantUsername;

    @Column(name = "key")
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private RoomStatus status;

    @OneToMany(mappedBy = "room", orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Message> messages;
}