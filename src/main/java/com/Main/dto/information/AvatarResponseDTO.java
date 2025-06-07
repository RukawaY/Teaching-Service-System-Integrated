package com.Main.dto.information;

public class AvatarResponseDTO {
    private String avatarPath;
    
    public AvatarResponseDTO() {
    }
    
    public AvatarResponseDTO(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    
    public String getAvatarPath() {
        return avatarPath;
    }
    
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
} 