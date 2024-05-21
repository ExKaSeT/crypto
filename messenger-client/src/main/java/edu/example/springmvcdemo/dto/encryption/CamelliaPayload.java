package edu.example.springmvcdemo.dto.encryption;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.round_key.CamelliaKeyGenerator;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamelliaPayload implements Serializable {
    @NotNull
    private CamelliaKeyGenerator.CamelliaKeySize camelliaKeySize;
}
