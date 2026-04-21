package com.ot.dto.ward;

import lombok.Data;

@Data
public class CompleteWardTaskRequest {
    private String completionNotes;   // optional — "BP 120/80 aaya", etc.
    private String readingValue;      // optional — vitals ke liye
    private String readingUnit;       // optional — "mmHg", "°F"
}