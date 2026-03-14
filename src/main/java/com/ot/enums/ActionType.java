package com.ot.enums;

public enum ActionType {
    // Existing
    CREATE, READ, UPDATE, DELETE,
    LOGIN, LOGOUT,
    EXPORT, PRINT,
    ASSIGN, TRANSFER,

    // OT Specific — ADD karo
    SCHEDULE,       // Operation schedule kiya
    CANCEL,         // Operation cancel kiya
    START,          // Surgery start ki
    END,            // Surgery end ki
    APPROVE,        // OT Request approve ki
    REJECT,         // OT Request reject ki
    UNASSIGN,       // Staff/Surgeon unassign kiya
    STATUS_CHANGE,  // Kisi bhi entity ka status change
}