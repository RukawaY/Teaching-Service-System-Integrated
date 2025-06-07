package com.Main.entity.information;

public class Classroom {
    private int classroomId = -1;
    private String location;
    private int capacity = 0;

    public int getId() {
        return classroomId;
    }

    public void setId(int classroomId) {
        this.classroomId = classroomId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return String.format("Classroom[id=%d, location='%s', capacity=%d]", getId(), getLocation(), getCapacity());
    }
}
