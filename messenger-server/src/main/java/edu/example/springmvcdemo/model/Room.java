package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rooms_seq")
    @SequenceGenerator(name = "rooms_seq", sequenceName = "rooms_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, name = "last_message_id")
    private Long lastMessageId;

    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomUser> roomUsers;
}