package com.ot.constants;

import com.ot.enums.RoleType;
import java.util.Set;

public class OTRoleConstants {

    private OTRoleConstants() {} // instantiation rokne ke liye

    public static final Set<RoleType> ALLOWED_ASSESSOR_ROLES = Set.of(
    	RoleType.ADMIN,
        RoleType.ANESTHESIOLOGIST,
        RoleType.SURGEON,
        RoleType.RESIDENT,
        RoleType.SCRUB_NURSE,
        RoleType.ANESTHESIA_NURSE
    );

    public static final Set<RoleType> ALLOWED_SURGEON_ROLES = Set.of(
        RoleType.SURGEON,
        RoleType.RESIDENT
    );

    public static final Set<RoleType> ALLOWED_STAFF_ROLES = Set.of(
        RoleType.SCRUB_NURSE,
        RoleType.CIRCULATING_NURSE,
        RoleType.ANESTHESIA_NURSE,
        RoleType.OT_TECHNICIAN,
        RoleType.SURGICAL_TECH,
        RoleType.ANESTHESIA_TECHNICIAN,
        RoleType.ORDERLY,
        RoleType.OT_ASSISTANT
    );
}