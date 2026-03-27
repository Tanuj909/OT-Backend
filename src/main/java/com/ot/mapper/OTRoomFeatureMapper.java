package com.ot.mapper;

import org.springframework.stereotype.Component;

import com.ot.dto.otRoom.OTRoomFeatureRequest;
import com.ot.dto.otRoom.OTRoomFeatureResponse;
import com.ot.entity.OTRoomFeature;

@Component
public class OTRoomFeatureMapper {

    public OTRoomFeature toEntity(OTRoomFeatureRequest request) {
        return OTRoomFeature.builder()
                .name(request.getName())
                .isActive(true)
                .build();
    }

    public OTRoomFeatureResponse toResponse(OTRoomFeature feature) {
        return OTRoomFeatureResponse.builder()
                .id(feature.getId())
                .name(feature.getName())
                .isActive(feature.getIsActive())
                .build();
    }
}