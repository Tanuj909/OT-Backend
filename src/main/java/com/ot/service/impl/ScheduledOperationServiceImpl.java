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
import org.springframework.stereotype.Service;
import com.ot.dto.ipdRequest.IpdOtRequest;
import com.ot.entity.Hospital;
import com.ot.entity.ScheduledOperation;
import com.ot.enums.OperationStatus;
import com.ot.enums.ProcedureComplexity;
import com.ot.exception.BadRequestException;
import com.ot.repository.HospitalRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.service.ScheduledOperationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduledOperationServiceImpl implements ScheduledOperationService{

    private final ScheduledOperationRepository operationRepository;

    private final HospitalRepository hospitalRepository;
    
    private String generateMrn() {
        long count = operationRepository.countTodayOperations() + 1;

        String date = java.time.LocalDate.now().toString().replace("-", "");

        return "MRN-" + date + "-" + String.format("%04d", count);
    }

    @Override
    public void createOperationFromIpd(IpdOtRequest request) {
    	
        if (request.getHospitalId() == null) {
            throw new BadRequestException("Hospital Id must not be null");
        }
        
        /* ================= FETCH HOSPITAL ================= */
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        ScheduledOperation operation = ScheduledOperation.builder()
                .hospital(hospital)
                .operationReference(UUID.randomUUID().toString())
                .patientId(request.getPatientId())
                .patientName(request.getPatientName())
                .patientMrn(generateMrn())
                .ipdAdmissionId(
                	    request.getAdmissionId() != null 
                	        ? request.getAdmissionId().toString()
                	        : null
                	)
                .procedureName(request.getProcedureName())
                .complexity(
                        request.getComplexity() != null
                                ? ProcedureComplexity.valueOf(request.getComplexity().toUpperCase())
                                : null
                    )
                .scheduledStartTime(request.getOperationDate()) // optional mapping
                .status(OperationStatus.REQUESTED)
                .createdBy("IPD_SERVICE")
                .build();

        operationRepository.save(operation);
    }
}