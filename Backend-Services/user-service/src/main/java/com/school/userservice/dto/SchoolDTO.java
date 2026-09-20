package com.school.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class SchoolDTO {

    private Long id;

    private String schoolCode;
    
    private String schoolName;

    private String address;

    private String phone;

    private String email;

    private String website;

    private String principalName;

    private String announcement;

    private byte[] logo;

    private byte[] favicon;

    private byte[] banner;

    private String keywords;

    @Builder.Default
    private boolean isActive = true;

}
