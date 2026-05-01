package com.ot.service.impl;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.operationtheater.OperationTheaterCreateRequest;
import com.ot.dto.operationtheater.OperationTheaterResponse;
import com.ot.dto.otRoom.OTRoomResponse;
import com.ot.entity.Hospital;
import com.ot.entity.OperationTheater;
import com.ot.entity.User;
import com.ot.enums.RoleType;
import com.ot.enums.TheaterStatus;
import com.ot.exception.UnauthorizedException;
import com.ot.mapper.OtRoomMapper;
import com.ot.repository.OTRoomRepository;
import com.ot.repository.OperationTheaterRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.OperationTheaterService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperationTheaterServiceImpl implements OperationTheaterService {

    private final OperationTheaterRepository repository;
    private final OTRoomRepository otRoomRepository; // inject karo upar
    
	public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal(); 
        return cud.getUser(); 
    }

    @Override
    public OperationTheaterResponse create(OperationTheaterCreateRequest request) {
    	
    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can create the Operation Theater!");
    	}

        Hospital hospital = admin.getHospital();

        OperationTheater ot = OperationTheater.builder()
                .hospital(hospital)
                .theaterNumber(request.getTheaterNumber())
                .name(request.getName())
                .createdBy(admin.getId().toString())
                .location(request.getLocation())
                .building(request.getBuilding())
                .floor(request.getFloor())
                .type(request.getType())
                .status(TheaterStatus.ACTIVE)
                .build();

        repository.save(ot);

        return mapToResponse(ot);
    }

    @Override
    public List<OperationTheaterResponse> getAll() {

    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can can Access Hospitals Operation Theater!");
    	}

        Hospital hospital = admin.getHospital();

        return repository.findByHospitalId(hospital.getId())
                .stream()
//                .filter(t -> t.getStatus() == TheaterStatus.ACTIVE)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public OperationTheaterResponse getById(Long id) {

    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can can Access Operation Theater!");
    	}

        Hospital hospital = admin.getHospital();

        OperationTheater ot = repository.findByIdAndHospitalId(id, hospital.getId())
                .orElseThrow(() -> new RuntimeException("Operation Theater not found"));

        return mapToResponse(ot);
    }
    
    @Override
    public List<OperationTheaterResponse> getActiveTheaters() {

        User admin = currentUser();
        Long hospitalId = admin.getHospital().getId();

        List<OperationTheater> theaters =
                repository.findByStatusAndHospitalId(
                        TheaterStatus.ACTIVE,
                        hospitalId
                );

        return theaters.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public OperationTheaterResponse update(Long id, OperationTheaterCreateRequest request) {

    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can can Update Operation Theater!");
    	}

        Hospital hospital = admin.getHospital();

        OperationTheater ot = repository.findByIdAndHospitalId(id, hospital.getId())
                .orElseThrow(() -> new RuntimeException("Operation Theater not found"));

        ot.setName(request.getName());
        ot.setLocation(request.getLocation());
        ot.setBuilding(request.getBuilding());
        ot.setFloor(request.getFloor());
        ot.setType(request.getType());
        ot.setUpdatedBy(admin.getId().toString());

        repository.save(ot);

        return mapToResponse(ot);
    }
    
    
    @Override
    public OperationTheaterResponse updateStatus(Long id, TheaterStatus status) {

        User admin = currentUser();

        if (admin.getRole() != RoleType.ADMIN) {
            throw new UnauthorizedException("Only Admin can update OT status!");
        }

        Long hospitalId = admin.getHospital().getId();

        OperationTheater ot = repository.findByIdAndHospitalId(id, hospitalId)
                .orElseThrow(() -> new RuntimeException("Operation Theater not found"));

        ot.setStatus(status);
        ot.setUpdatedBy(admin.getId().toString());

        repository.save(ot);

        return mapToResponse(ot);
    }
    
    @Override
    public List<OTRoomResponse> getRoomsByTheater(Long theaterId) {

        User admin = currentUser();

        if (admin.getRole() != RoleType.ADMIN) {
            throw new UnauthorizedException("Only Admin can access OT Rooms!");
        }

        Hospital hospital = admin.getHospital();

        // Theater verify - is hospital ka hai?
        repository.findByIdAndHospitalId(theaterId, hospital.getId())
                .orElseThrow(() -> new RuntimeException("Operation Theater not found!"));

        return otRoomRepository
                .findByOperationTheaterIdAndOperationTheaterHospitalId(theaterId, hospital.getId())
                .stream()
                .map(OtRoomMapper::mapRoomToResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {

    	User admin = currentUser();
    	
    	if(admin.getRole()!=RoleType.ADMIN) {
    		throw new UnauthorizedException("Only Admin Can can Delete Operation Theater!");
    	}

        Hospital hospital = admin.getHospital();

        OperationTheater ot = repository.findByIdAndHospitalId(id, hospital.getId())
                .orElseThrow(() -> new RuntimeException("Operation Theater not found"));

        repository.delete(ot);
    }

    private OperationTheaterResponse mapToResponse(OperationTheater ot) {

        return OperationTheaterResponse.builder()
                .id(ot.getId())
                .name(ot.getName())
                .theaterNumber(ot.getTheaterNumber())
                .location(ot.getLocation())
                .building(ot.getBuilding())
                .floor(ot.getFloor())
                .type(ot.getType())
                .status(ot.getStatus())
                .build();
    }

}
