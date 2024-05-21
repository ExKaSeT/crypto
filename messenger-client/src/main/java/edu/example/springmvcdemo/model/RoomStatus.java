package edu.example.springmvcdemo.model;

public enum RoomStatus {
    CREATED, // create room and waiting open key from participant
    TO_AGREE, // incoming room request
    AGREED
}
