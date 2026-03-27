package com.ot.dto.otRoom;

import java.util.List;

import lombok.Data;

@Data
public class FeatureMappingRequest {
    private List<Long> featureIds;
}
