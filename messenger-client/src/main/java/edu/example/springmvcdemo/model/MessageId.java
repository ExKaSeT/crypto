package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MessageId implements Serializable {

    @Column(nullable = false, name = "id")
    private Long id;

    @Column(nullable = false, name = "room_id")
    private Long roomId;
}