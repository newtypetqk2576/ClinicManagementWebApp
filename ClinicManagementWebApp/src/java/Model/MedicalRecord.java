package Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecord {
    private int medicalRecordID;
    private int patientID;
    private int doctorID;
    private Integer appointmentID;
    private String patientName;
    private String doctorName;
    private LocalDateTime visitDate;
    private String symptoms;
    private String diagnosis;
    private String treatmentNotes;
    private String status;
    private LocalDateTime createdAt;
    private final List<ServiceLine> services = new ArrayList<>();
    private final List<Prescription> prescriptions = new ArrayList<>();
    private Invoice invoice;

    public int getMedicalRecordID() {
        return medicalRecordID;
    }

    public void setMedicalRecordID(int medicalRecordID) {
        this.medicalRecordID = medicalRecordID;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(int doctorID) {
        this.doctorID = doctorID;
    }

    public Integer getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(Integer appointmentID) {
        this.appointmentID = appointmentID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDateTime getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDateTime visitDate) {
        this.visitDate = visitDate;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentNotes() {
        return treatmentNotes;
    }

    public void setTreatmentNotes(String treatmentNotes) {
        this.treatmentNotes = treatmentNotes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ServiceLine> getServices() {
        return services;
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public static class ServiceLine {
        private int serviceID;
        private String serviceName;
        private int quantity;
        private BigDecimal unitPrice;
        private String notes;

        public int getServiceID() {
            return serviceID;
        }

        public void setServiceID(int serviceID) {
            this.serviceID = serviceID;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class Prescription {
        private int prescriptionID;
        private LocalDateTime prescriptionDate;
        private String notes;
        private String status;
        private final List<MedicineLine> medicines = new ArrayList<>();

        public int getPrescriptionID() {
            return prescriptionID;
        }

        public void setPrescriptionID(int prescriptionID) {
            this.prescriptionID = prescriptionID;
        }

        public LocalDateTime getPrescriptionDate() {
            return prescriptionDate;
        }

        public void setPrescriptionDate(LocalDateTime prescriptionDate) {
            this.prescriptionDate = prescriptionDate;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<MedicineLine> getMedicines() {
            return medicines;
        }
    }

    public static class MedicineLine {
        private int medicineID;
        private String medicineName;
        private String activeIngredient;
        private String unit;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private int quantity;
        private BigDecimal unitPrice;
        private String instructions;

        public int getMedicineID() {
            return medicineID;
        }

        public void setMedicineID(int medicineID) {
            this.medicineID = medicineID;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public void setMedicineName(String medicineName) {
            this.medicineName = medicineName;
        }

        public String getActiveIngredient() {
            return activeIngredient;
        }

        public void setActiveIngredient(String activeIngredient) {
            this.activeIngredient = activeIngredient;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getDosage() {
            return dosage;
        }

        public void setDosage(String dosage) {
            this.dosage = dosage;
        }

        public String getFrequency() {
            return frequency;
        }

        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }

        public Integer getDurationDays() {
            return durationDays;
        }

        public void setDurationDays(Integer durationDays) {
            this.durationDays = durationDays;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getInstructions() {
            return instructions;
        }

        public void setInstructions(String instructions) {
            this.instructions = instructions;
        }
    }

    public static class Invoice {
        private int invoiceID;
        private LocalDateTime invoiceDate;
        private BigDecimal serviceAmount;
        private BigDecimal medicineAmount;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String paymentMethod;
        private String paymentStatus;
        private String notes;

        public int getInvoiceID() {
            return invoiceID;
        }

        public void setInvoiceID(int invoiceID) {
            this.invoiceID = invoiceID;
        }

        public LocalDateTime getInvoiceDate() {
            return invoiceDate;
        }

        public void setInvoiceDate(LocalDateTime invoiceDate) {
            this.invoiceDate = invoiceDate;
        }

        public BigDecimal getServiceAmount() {
            return serviceAmount;
        }

        public void setServiceAmount(BigDecimal serviceAmount) {
            this.serviceAmount = serviceAmount;
        }

        public BigDecimal getMedicineAmount() {
            return medicineAmount;
        }

        public void setMedicineAmount(BigDecimal medicineAmount) {
            this.medicineAmount = medicineAmount;
        }

        public BigDecimal getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}
