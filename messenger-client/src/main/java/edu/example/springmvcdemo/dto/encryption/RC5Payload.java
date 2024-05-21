package edu.example.springmvcdemo.dto.encryption;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RC5Payload implements Serializable {
    @NotNull
    private RC5WordLengthBytes wordLengthBytes;
    @NotNull
    @Length(min = 1, max = 255, message = "Кол-во раундов должно быть от 1 до 255")
    private int roundCount;
    @NotNull
    @Length(min = 1, max = 255, message = "Длина ключа должна быть от 1 до 255")
    private int keyLength;
}
