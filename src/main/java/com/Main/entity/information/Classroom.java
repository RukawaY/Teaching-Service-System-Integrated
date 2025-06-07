package com.Main.entity.information;

public class Classroom {
    private int classroom_id = -1;
    private String classroom_location;
    private int classroom_capacity = 0;

    public int getClassroom_id() {
        return classroom_id;
    }

    public void setClassroom_id(int classroomId) {
        this.classroom_id = classroomId;
    }

    public String getClassroom_location() {
        return classroom_location;
    }

    public void setClassroom_location(String location) {
        this.classroom_location = location;
    }

    public int getClassroom_capacity() {
        return classroom_capacity;
    }

    public void setClassroom_capacity(int capacity) {
        this.classroom_capacity = capacity;
    }

    @Override
    public String toString() {
        return String.format("Classroom[classroom_id=%d, classroom_location='%s', classroom_capacity=%d]", getClassroom_id(), getClassroom_location(), getClassroom_capacity());
    }
}
