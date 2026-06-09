package com.systemankiet.dto;

import com.systemankiet.entity.Organization;
import lombok.Data;

@Data
public class OrganizationDto {

    private Long id;
    private String name;

    public static OrganizationDto fromEntity(Organization org) {
        OrganizationDto dto = new OrganizationDto();
        dto.setId(org.getId());
        dto.setName(org.getName());
        return dto;
    }
}
