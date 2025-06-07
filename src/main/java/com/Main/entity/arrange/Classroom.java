package com.Main.entity.arrange;

public class Classroom {
    private int classroomId = -1;
    private String location;
    private int capacity = 0;

    //newly added
    private String category;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    @Override
    public String toString() {
        return String.format("Classroom[id=%d, location='%s', capacity=%d, category='%s']", getId(), getLocation(), getCapacity(), getCategory());
    }
}
