package edu.example.springmvcdemo.mapper;

import edu.example.springmvcdemo.dto.message.MessageResponseDto;
import edu.example.springmvcdemo.dto.messages_kafka.MessageDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageResponseDto messageDtoToMessageResponseDto(MessageDto messageDto, Long epochTime);
}
