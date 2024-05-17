package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "rooms_users")
@Data
public class RoomUser {
    @EmbeddedId
    private RoomUserId id;

    @Column(nullable = false, name = "is_agreed")
    private boolean isAgreed;

    @Column(name = "message_offset")
    private Long messageOffset;

    @ManyToOne
    @MapsId("roomId")
    @JoinColumn(nullable = false, name = "room_id")
    private Room room;

    @ManyToOne
    @MapsId("userUsername")
    @JoinColumn(nullable = false, name = "user_username")
    private User user;
}

