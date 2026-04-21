ALTER TABLE ward_admissions
ADD COLUMN assigned_staff_id BIGINT,
ADD COLUMN assigned_staff_name VARCHAR(255),
ADD COLUMN staff_assigned_at DATETIME;

CREATE TABLE ward_tasks(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    operation_id BIGINT NOT NULL,
    ward_admission_id BIGINT NOT NULL,
    hospital_id BIGINT NOT NULL,
    
    patient_id VARCHAR(200),
    patient_name VARCHAR(255),
    patient_mrn VARCHAR(255),
    
    task_type VARCHAR(50) NOT NULL,
    task_description VARCHAR(255) NOT NULL,
    task_notes VARCHAR(255) ,
    
    scheduled_time DATETIME,
    is_recurring BOOLEAN DEFAULT FALSE,
    interval_hours INT,
    recurring_end_time DATETIME,
    
    assigned_by_id BIGINT NOT NULL,
    assigned_by_name VARCHAR(255),
    assigned_at DATETIME,
    
    status VARCHAR(50) DEFAULT 'PENDING',
    
    completed_by_id BIGINT,
    completed_by_name VARCHAR(255),
    completed_at DATETIME,
    
    completion_notes VARCHAR(1000),

    reading_value VARCHAR(255),
    reading_unit VARCHAR(255),

    created_at DATETIME,
    updated_at DATETIME,
    
    FOREIGN KEY (operation_id) REFERENCES scheduled_operations(id),
    FOREIGN KEY (ward_admission_id) REFERENCES ward_admissions(id),
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id) 
);