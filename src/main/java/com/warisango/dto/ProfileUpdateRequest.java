package com.warisango.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank(message = "Display name is required.")
    @Size(min = 3, max = 30, message = "Display name must be between 3 and 30 characters.")
    @Pattern(regexp = "[A-Za-z0-9 ]+", message = "Display name may contain only letters, numbers, and spaces.")
    private String displayName;

    @Size(max = 40, message = "Gender must not exceed 40 characters.")
    private String gender;

    @Size(max = 500, message = "About me must not exceed 500 characters.")
    private String aboutMe;


    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAboutMe() { return aboutMe; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }
}
