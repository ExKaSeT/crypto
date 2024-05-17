package edu.example.springmvcdemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomUserId implements Serializable {
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "user_username")
    private String userUsername;
}