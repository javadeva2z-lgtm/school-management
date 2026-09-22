package com.school.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class SchoolPartialDTO {

    private Long id;

    private String schoolCode;
    
    private String schoolName;

    private String address;

    private String phone;

    private String email;

    private String website;

    private String principalName;

    private String announcement;
    
    private String keywords;

    @Builder.Default
    private Boolean isActive = true;

}
