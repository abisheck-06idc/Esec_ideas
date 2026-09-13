package com.college.incubation.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String phoneNumber;

    @Size(max = 100)
    private String department;

    // Base64 data URL, for example: data:image/jpeg;base64,...
    @Size(max = 8_000_000)
    private String profilePhoto;
}
