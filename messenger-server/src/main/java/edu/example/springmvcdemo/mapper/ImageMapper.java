//package edu.example.springmvcdemo.mapper;
//
//import edu.example.springmvcdemo.dto.image.ImageResponseDto;
//import edu.example.springmvcdemo.dto.image.UploadImageResponseDto;
//import edu.example.springmvcdemo.model.Image;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//@Mapper(componentModel = "spring")
//public interface ImageMapper {
//    @Mapping(target = "imageId", source = "link")
//    UploadImageResponseDto toUploadImageResponseDto(Image image);
//
//
//    @Mapping(target = "imageId", source = "link")
//    @Mapping(target = "filename", source = "originalName")
//    @Mapping(target = "size", source = "sizeBytes")
//    ImageResponseDto toImageResponseDto(Image image);
//}
