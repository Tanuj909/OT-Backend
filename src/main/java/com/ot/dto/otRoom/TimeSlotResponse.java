package com.ot.dto.otRoom;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeSlotResponse {
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean available;
}