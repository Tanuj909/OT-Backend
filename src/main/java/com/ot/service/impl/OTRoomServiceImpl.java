package com.ot.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ot.dto.otRoom.FeatureMappingRequest;
import com.ot.dto.otRoom.OTRoomCreateRequest;
import com.ot.dto.otRoom.OTRoomFeatureResponse;
import com.ot.dto.otRoom.OTRoomResponse;
import com.ot.dto.otRoom.TimeSlotResponse;
import com.ot.dto.otRoom.UpdateRoomStatusRequest;
import com.ot.entity.Hospital;
import com.ot.entity.OTRoom;
import com.ot.entity.OTRoomFeature;
import com.ot.entity.OperationTheater;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.RoleType;
import com.ot.enums.RoomStatus;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.mapper.OTRoomFeatureMapper;
import com.ot.repository.OTRoomFeatureRepository;
import com.ot.repository.OTRoomRepository;
import com.ot.repository.OperationTheaterRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.OTRoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OTRoomServiceImpl implements OTRoomService {

    private final OTRoomRepository roomRepository;
    private final OperationTheaterRepository theaterRepository;
    private final OTRoomFeatureRepository featureRepository;
    private final OTRoomFeatureMapper featureMapper;
    private final ScheduledOperationRepository operationRepository;
    
	public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal(); 
        return cud.getUser(); 
    }

    @Override
    public OTRoomResponse create(OTRoomCreateRequest request) {

    	User admin = currentUser();

    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can create the OT Room!");
    	}

        Hospital hospital = admin.getHospital();

        OperationTheater theater = theaterRepository
                .findById(request.getOperationTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation Theater not found"));

        //Check if the OperationTheater Hospital Id and Admin's Hospital Id Match!
        if(!theater.getHospital().getId().equals(hospital.getId())) {
        	throw new UnauthorizedException("This Theater Does not Belong to Your Hospital!");
        }

        OTRoom room = OTRoom.builder()
                .hospital(hospital)
                .operationTheater(theater)
                .roomNumber(request.getRoomNumber())
                .roomName(request.getRoomName())
                .location(request.getLocation())
                .floor(request.getFloor())
                .type(request.getType())
                .status(request.getStatus())
                .hasHvac(request.getHasHvac())
                .hasGasSupply(request.getHasGasSupply())
                .hasSuction(request.getHasSuction())
                .hasEmergencyPower(request.getHasEmergencyPower())
                .capacity(request.getCapacity())
                .specialFeatures(request.getSpecialFeatures())
                .build();

        roomRepository.save(room);

        return map(room);
    }

    @Override
    public List<OTRoomResponse> getAll() {

    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can Get the OT Room!");
    	}

        Hospital hospital = admin.getHospital();

        return roomRepository.findByHospitalId(hospital.getId())
                .stream()
                .map(this::map)
                .toList();
    }
    
    @Override
    @Transactional
    public void mapFeatures(Long roomId, FeatureMappingRequest request) {

        OTRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        Set<OTRoomFeature> features = new HashSet<>(
                featureRepository.findAllById(request.getFeatureIds())
        );

        // existing features preserve + add new
        if (room.getFeatures() == null) {
            room.setFeatures(features);
        } else {
            room.getFeatures().addAll(features);
        }

        roomRepository.save(room);
    }
    
    @Override
    @Transactional
    public void unmapFeatures(Long roomId, FeatureMappingRequest request) {

        OTRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        Set<OTRoomFeature> featuresToRemove = new HashSet<>(
                featureRepository.findAllById(request.getFeatureIds())
        );

        if (room.getFeatures() != null) {
            room.getFeatures().removeAll(featuresToRemove);
        }

        roomRepository.save(room);
    }
    
    @Override
    public List<OTRoomFeatureResponse> getRoomFeatures(Long roomId) {

        OTRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        return room.getFeatures()
                .stream()
                .map(featureMapper::toResponse)
                .toList();
    }

    @Override
    public List<OTRoomResponse> getByTheater(Long theaterId) {
    	
    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can Get the OT Room!");
    	}
    	
    	//Fetch Hospital
    	Hospital hospital = admin.getHospital();
    	
        OperationTheater theater = theaterRepository
                .findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation Theater not found"));
    	
    	//Get Theater HospitalId
        if(!theater.getHospital().getId().equals(hospital.getId())) {
        	throw new UnauthorizedException("Not Allowed to View the Rooms!");
        }
    	
        return roomRepository.findByOperationTheaterId(theaterId)
                .stream()
                .map(this::map)
                .toList();
    }
    
    
//    @Override
//    public List<TimeSlotResponse> getRoomTimeSlots(Long roomId, LocalDate date) {
//
//        User admin = currentUser();
//
//        if (admin.getRole() != RoleType.ADMIN) {
//            throw new UnauthorizedException("Only Admin can access!");
//        }
//
//        OTRoom room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
//
//        // 🔥 define day range
//        LocalDateTime dayStart = date.atStartOfDay();         // 00:00
//        LocalDateTime dayEnd = date.atTime(23, 59);           // 23:59
//
//        // 🔥 cleaning buffer
//        int bufferMinutes = 60;
//
//        // 🔥 fetch all operations for that day
//        List<ScheduledOperation> operations =
//                operationRepository.findByRoomAndDate(roomId, dayStart, dayEnd);
//
//        List<TimeSlotResponse> slots = new ArrayList<>();
//
//        // 🔥 slot size = 30 mins
//        int slotMinutes = 30;
//
//        for (LocalDateTime slotStart = dayStart;
//             slotStart.isBefore(dayEnd);
//             slotStart = slotStart.plusMinutes(slotMinutes)) {
//
//            LocalDateTime slotEnd = slotStart.plusMinutes(slotMinutes);
//
//            boolean isAvailable = true;
//
//            for (ScheduledOperation op : operations) {
//
//                LocalDateTime opStart = op.getScheduledStartTime();
//                LocalDateTime opEnd = op.getScheduledEndTime();
//
//                if (opStart == null || opEnd == null) continue;
//
//                // 👉 apply cleaning buffer
//                LocalDateTime blockedEnd = opEnd.plusMinutes(bufferMinutes);
//
//                // 🔥 OVERLAP CHECK (same as before)
//                boolean overlap =
//                        opStart.isBefore(slotEnd) &&
//                        blockedEnd.isAfter(slotStart);
//
//                if (overlap) {
//                    isAvailable = false;
//                    break;
//                }
//            }
//
//            slots.add(new TimeSlotResponse(slotStart, slotEnd, isAvailable));
//        }
//
//        return slots;
//    }
    
    @Override
    public OTRoomResponse getById(Long id) {

    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can create the OT Room!");
    	}

        Hospital hospital = admin.getHospital();


        OTRoom room = roomRepository
                .findByIdAndHospitalId(id, hospital.getId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return map(room);
    }

    @Override
    public OTRoomResponse update(Long id, OTRoomCreateRequest request) {

    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can create the OT Room!");
    	}

        Hospital hospital = admin.getHospital();

        OTRoom room = roomRepository
                .findByIdAndHospitalId(id, hospital.getId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setRoomNumber(request.getRoomNumber());
        room.setRoomName(request.getRoomName());
        room.setLocation(request.getLocation());
        room.setFloor(request.getFloor());
        room.setType(request.getType());
        room.setStatus(request.getStatus());
        room.setHasEmergencyPower(request.getHasEmergencyPower());
        room.setHasGasSupply(request.getHasGasSupply());
        room.setHasHvac(request.getHasHvac());
        room.setHasSuction(request.getHasSuction());
        room.setCapacity(request.getCapacity());
        room.setSpecialFeatures(request.getSpecialFeatures());

        roomRepository.save(room);

        return map(room);
    }
    
    
    @Override
    public OTRoomResponse updateStatus(Long id, UpdateRoomStatusRequest request) {

    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can create the OT Room!");
    	}

        Hospital hospital = admin.getHospital();

        OTRoom room = roomRepository
                .findByIdAndHospitalId(id, hospital.getId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setStatus(request.getStatus());
        room.setStatusReason(request.getReason());

        roomRepository.save(room);

        return map(room);
    }
    
    @Override
    public List<OTRoomResponse> getAvailableRooms() {

    	User admin = currentUser();

    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can create the OT Room!");
    	}

        Hospital hospital = admin.getHospital();

        return roomRepository
                .findByHospitalIdAndStatus(hospital.getId(), RoomStatus.AVAILABLE)
                .stream()
                .map(this::map)
                .toList();
    }
    
    @Override
    public void enableRoom(Long id) {

    	User admin = currentUser();

    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can create the OT Room!");
    	}

        Hospital hospital = admin.getHospital();

        OTRoom room = roomRepository
                .findByIdAndHospitalId(id, hospital.getId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setIsActive(true);

        roomRepository.save(room);
    }

    @Override
    public void disableRoom(Long id) {

    	User admin = currentUser();

    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can create the OT Room!");
    	}

        Hospital hospital = admin.getHospital();

        OTRoom room = roomRepository
                .findByIdAndHospitalId(id, hospital.getId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setIsActive(false);

        roomRepository.save(room);
    }

    @Override
    public void delete(Long id) {

    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can create the OT Room!");
    	}

        Hospital hospital = admin.getHospital();

        OTRoom room = roomRepository
                .findByIdAndHospitalId(id, hospital.getId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        roomRepository.delete(room);
    }

    private OTRoomResponse map(OTRoom room) {

        return OTRoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomName(room.getRoomName())
                .location(room.getLocation())
                .floor(room.getFloor())
                .type(room.getType())
                .status(room.getStatus())
                .isActive(room.getIsActive())
                .capacity(room.getCapacity())
                .hasHvac(room.getHasHvac())
                .hasGasSupply(room.getHasGasSupply())
                .hasSuction(room.getHasSuction())
                .hasEmergencyPower(room.getHasEmergencyPower())
                .specialFeatures(room.getSpecialFeatures())
                .operationTheaterId(room.getOperationTheater().getId())
                .build();
    }
}
