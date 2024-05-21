package edu.example.springmvcdemo.dto.room;

import edu.example.springmvcdemo.dto.encryption.CamelliaPayload;
import edu.example.springmvcdemo.dto.encryption.EncryptionPayload;
import edu.example.springmvcdemo.dto.encryption.RC5Payload;
import edu.example.springmvcdemo.model.EncryptionType;
import edu.example.springmvcdemo.model.Room;
import edu.example.springmvcdemo.model.RoomStatus;
import edu.example.springmvcdemo.service.SerializationUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.util.Objects.nonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto {
    private Long roomId;
    private String participantUsername;
    private RoomStatus status;
    private String encryptionInfo;

    public static RoomDto fromRoom(Room room) {
        var dto = new RoomDto();
        dto.setRoomId(room.getRoomId());
        dto.setStatus(room.getStatus());
        dto.setParticipantUsername(room.getParticipantUsername());
        if (nonNull(room.getEncryptionPayload())) {
            String encryptionInfo;
            var encryption = (EncryptionPayload) SerializationUtils.deserialize(room.getEncryptionPayload());
            if (encryption.getEncryptionType().equals(EncryptionType.CAMELLIA)) {
                encryptionInfo = "Camellia " + ((CamelliaPayload) encryption.getPayload()).getCamelliaKeySize().toString();
            } else {
                var RC5Payload = (RC5Payload) encryption.getPayload();
                encryptionInfo = String.format("RC5 %s Кол-во раундов: %s Длина ключа: %s",
                        RC5Payload.getWordLengthBytes(), RC5Payload.getRoundCount(), RC5Payload.getKeyLength());
            }
            dto.setEncryptionInfo(encryptionInfo);
        }
        return dto;
    }
}
