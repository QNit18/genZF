package com.qnit18.main_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthUserResponse {
    String id;
    String username;
    String firstName;
    String lastName;
    LocalDate dob;
    Set<String> roles; // Simplified to String set for easier deserialization
}

