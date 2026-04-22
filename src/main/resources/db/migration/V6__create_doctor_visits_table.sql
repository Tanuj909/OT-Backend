CREATE TABLE doctor_visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    operation_id BIGINT NOT NULL,
    ward_admission_id BIGINT NOT NULL,
    hospital_id BIGINT NOT NULL,

    patient_id VARCHAR(255),
    patient_name VARCHAR(255),
    patient_mrn VARCHAR(255),

    visit_time DATETIME,

    doctor_id BIGINT,
    doctor_name VARCHAR(255),
    doctor_specialization VARCHAR(255),

    recorded_by_id BIGINT,
    recorded_by_name VARCHAR(255),

    clinical_observations VARCHAR(2000),
    diagnosis VARCHAR(2000),
    treatment_plan VARCHAR(2000),

    has_medication_change BOOLEAN DEFAULT FALSE,
    medications_added VARCHAR(2000),
    medications_discontinued VARCHAR(2000),
    medication_notes VARCHAR(2000),

    next_visit_scheduled DATETIME,
    next_visit_instructions VARCHAR(500),

    discharge_recommended BOOLEAN DEFAULT FALSE,
    discharge_notes VARCHAR(1000),
    expected_discharge_date DATETIME,

    status VARCHAR(50) DEFAULT 'COMPLETED',

    created_at DATETIME,
    updated_at DATETIME,

    FOREIGN KEY (operation_id) REFERENCES scheduled_operations(id),
    FOREIGN KEY (ward_admission_id) REFERENCES ward_admissions(id),
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id)
);