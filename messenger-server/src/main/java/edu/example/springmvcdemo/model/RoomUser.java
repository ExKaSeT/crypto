package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms_users")
@Data
@NoArgsConstructor
public class RoomUser {
    @EmbeddedId
    private RoomUserId id = new RoomUserId();

    @Column(nullable = false, name = "is_agreed")
    private boolean isAgreed;

    @ManyToOne
    @MapsId("roomId")
    @JoinColumn(nullable = false, name = "room_id")
    private Room room;

    @ManyToOne
    @MapsId("userUsername")
    @JoinColumn(nullable = false, name = "user_username")
    private User user;
}

