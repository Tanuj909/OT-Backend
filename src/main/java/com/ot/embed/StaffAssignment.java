package com.ot.embed;

import lombok.*;

import com.ot.enums.StaffRole;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAssignment {

    private Long staffId;

    private String staffName;

    @Enumerated(EnumType.STRING)
    private StaffRole role;

    private String department;
}
