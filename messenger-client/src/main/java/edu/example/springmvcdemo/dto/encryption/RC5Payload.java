package edu.example.springmvcdemo.dto.encryption;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RC5Payload implements Serializable {
    @NotNull
    private RC5WordLengthBytes wordLengthBytes;
    @NotNull
    @Min(value = 1, message = "Кол-во раундов должно быть от 1 до 255")
    @Max(value = 255, message = "Кол-во раундов должно быть от 1 до 255")
    private Integer roundCount;
    @NotNull
    @Min(value = 1, message = "Длина ключа должна быть от 1 до 255")
    @Max(value = 255, message = "Длина ключа должна быть от 1 до 255")
    private Integer keyLength;
}
