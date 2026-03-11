package com.ot.embed;

import lombok.*;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurgeonAssignment {
    
    private Long surgeonId;
    private String surgeonName;
    
    @Enumerated(EnumType.STRING)
    private com.ot.enums.SurgeonRole role;
    
    private boolean isPrimary;
}
