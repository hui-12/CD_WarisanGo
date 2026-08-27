package com.warisango.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank(message = "Display name is required.")
    @Size(min = 3, max = 30, message = "Display name must be between 3 and 30 characters.")
    @Pattern(regexp = "[A-Za-z0-9 ]+", message = "Display name may contain only letters, numbers, and spaces.")
    private String displayName;


    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
