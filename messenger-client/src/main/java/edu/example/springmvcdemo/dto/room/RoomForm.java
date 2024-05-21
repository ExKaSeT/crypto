package edu.example.springmvcdemo.dto.room;

import edu.example.springmvcdemo.dto.encryption.CamelliaPayload;
import edu.example.springmvcdemo.dto.encryption.RC5Payload;
import edu.example.springmvcdemo.model.EncryptionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomForm {
    @NotNull(message = "Логин не может отсутствовать")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "В логине можно использовать только латинские символы и цифры")
    @Length(min = 5, max = 20, message = "Логин должен содержать от 5 до 20 символов")
    private String participantUsername;

    @NotNull
    private EncryptionType encryptionType;

    private CamelliaPayload camelliaPayload;

    private RC5Payload rc5Payload;
}