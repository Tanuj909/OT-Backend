//package com.ot.service.impl;
//
//import java.util.UUID;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import com.ot.dto.ipdRequest.IpdOtRequest;
//import com.ot.entity.Hospital;
//import com.ot.entity.ScheduledOperation;
//import com.ot.entity.User;
//import com.ot.enums.OperationStatus;
//import com.ot.exception.BadRequestException;
//import com.ot.repository.HospitalRepository;
//import com.ot.repository.ScheduledOperationRepository;
//import com.ot.security.CustomUserDetails;
//import com.ot.service.ScheduledOperationService;
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class ScheduledOperationServiceImpl implements ScheduledOperationService{
//
//    private final ScheduledOperationRepository operationRepository;
//
//    private final HospitalRepository hospitalRepository;
//    
//    private User currentUser() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || !auth.isAuthenticated()) {
//            throw new IllegalStateException("No user is logged in");
//        }
//        return ((CustomUserDetails) auth.getPrincipal()).getUser();
//    }
//
//
//    @Override
//    public void createOperationFromIpd(IpdOtRequest request) {
//
//    	//Check kro Hospital exist krta hai ya nhi!
//    	//Get Hospital ID of Cureent User:
//    
//    	if(request.getIpdHospitalId()==null) {
//    		throw new BadRequestException("Hospital Id Must not be Null");
//    	}
//        Hospital hospital = hospitalRepository.findById(request.getIpdHospitalId())
//                .orElseThrow(() -> new RuntimeException("Hospital not found"));
//
//        ScheduledOperation operation = ScheduledOperation.builder()
//                .hospital(hospital)
//                .operationReference(UUID.randomUUID().toString())
//                .patientId(request.getPatientId())
//                .patientName(request.getPatientName())
//                .patientMrn(request.getPatientMrn())
//                .ipdAdmissionId(request.getIpdAdmissionId().toString())
//                .procedureName(request.getProcedureName())
//                .procedureCode(request.getProcedureCode())
//                .status(OperationStatus.REQUESTED)
//                .createdBy("IPD_SERVICE")
//                .build();
//
//        operationRepository.save(operation);
//    }
//}

package com.ot.service.impl;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ot.dto.ipdRequest.IpdOtRequest;
import com.ot.entity.Hospital;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.OperationStatus;
import com.ot.exception.BadRequestException;
import com.ot.repository.HospitalRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.ScheduledOperationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduledOperationServiceImpl implements ScheduledOperationService{

    private final ScheduledOperationRepository operationRepository;

    private final HospitalRepository hospitalRepository;
    
    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }
    
    private String generateMrn() {
        long count = operationRepository.countTodayOperations() + 1;

        String date = java.time.LocalDate.now().toString().replace("-", "");

        return "MRN-" + date + "-" + String.format("%04d", count);
    }
    
//    private String generatePatientId() {
//        return UUID.randomUUID().toString();
//    }
    
    private Long generatePatientId() {
        long time = System.currentTimeMillis();
        int random = new java.util.Random().nextInt(999); // 0–999
        return Long.parseLong(time + "" + random);
    }


    @Override
    public void createOperationFromIpd(IpdOtRequest request) {

    	//Check kro Hospital exist krta hai ya nhi!
    	//Get Hospital ID of Cureent User:
    	User currentUser = currentUser();
    	
    	if(currentUser.getHospital().getId()==null) {
    		throw new BadRequestException("User Is not Assigned to Any Hospital");
    	}
    	
    	Long hospitalId = currentUser.getHospital().getId();
    

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        ScheduledOperation operation = ScheduledOperation.builder()
                .hospital(hospital)
                .operationReference(UUID.randomUUID().toString())
                .patientId(generatePatientId())
                .patientName(request.getPatientName())
                .patientMrn(generateMrn())
                .ipdAdmissionId(request.getIpdAdmissionId().toString())
                .procedureName(request.getProcedureName())
//                .procedureCode(request.getProcedureCode())
                .status(OperationStatus.REQUESTED)
                .createdBy("IPD_SERVICE")
                .build();

        operationRepository.save(operation);
    }
}