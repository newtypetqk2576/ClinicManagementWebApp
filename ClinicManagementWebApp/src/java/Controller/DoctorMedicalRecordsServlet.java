package Controller;

import Dal.AppointmentDAO;
import Dal.DoctorDAO;
import Dal.MedicalRecordDAO;
import Dal.MedicineDAO;
import Dal.PatientDAO;
import Dal.ServiceDAO;
import Model.Appointment;
import Model.Doctor;
import Model.MedicalRecord;
import Model.MedicalRecord.MedicineLine;
import Model.MedicalRecord.Prescription;
import Model.MedicalRecord.ServiceLine;
import Model.Medicine;
import Model.Patient;
import Model.Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {
    "/doctor/medical-records",
    "/doctor/medical-records/new",
    "/doctor/medical-records/search-patients",
    "/doctor/medical-records/search-services",
    "/doctor/medical-records/search-medicines"
})
public class DoctorMedicalRecordsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorMedicalRecordsServlet.class.getName());
    private static final String LIST_PAGE = "/view/doctor-medical-records.jsp";
    private static final String FORM_PAGE = "/view/doctor-medical-record-form.jsp";
    private static final Set<String> RECORD_STATUSES =
            Set.of("IN_PROGRESS", "COMPLETED", "CANCELLED");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        Integer doctorID = requireDoctor(request, response);
        if (doctorID == null) {
            return;
        }
        String path = request.getServletPath();
        if (path.endsWith("/search-patients")) {
            searchPatients(request, response);
            return;
        }
        if (path.endsWith("/search-services")) {
            searchServices(request, response);
            return;
        }
        if (path.endsWith("/search-medicines")) {
            searchMedicines(request, response);
            return;
        }

        try {
            Doctor doctor = loadSignedInDoctor(request, doctorID);
            request.setAttribute("doctor", doctor);
            HttpSession session = request.getSession(false);
            request.setAttribute("message", session.getAttribute("doctorMedicalRecordMessage"));
            session.removeAttribute("doctorMedicalRecordMessage");

            if ("/doctor/medical-records/new".equals(path)) {
                loadNewForm(request, response, doctor, null);
                return;
            }

            String idValue = request.getParameter("id");
            if (idValue == null) {
                request.setAttribute("records",
                        new MedicalRecordDAO(getServletContext()).getByDoctorID(doctorID));
                request.getRequestDispatcher(LIST_PAGE).forward(request, response);
                return;
            }

            int recordID = parseID(idValue);
            MedicalRecord record = new MedicalRecordDAO(getServletContext())
                    .getDetailsForDoctor(recordID, doctorID);
            if (record == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            renderEditForm(request, response, doctor, record, null);
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load Doctor medical records.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Medical records are temporarily unavailable.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        request.setCharacterEncoding("UTF-8");
        Integer doctorID = requireDoctor(request, response);
        if (doctorID == null) {
            return;
        }
        String action = request.getParameter("action");
        if (!"create".equals(action) && !"update".equals(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Doctor doctor = loadSignedInDoctor(request, doctorID);
            if (!doctor.isActive()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            if ("create".equals(action)) {
                createRecord(request, response, doctor);
            } else {
                updateRecord(request, response, doctor);
            }
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to save Doctor medical record.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The medical record could not be saved right now.");
        }
    }

    private void createRecord(HttpServletRequest request, HttpServletResponse response, Doctor doctor)
            throws ServletException, IOException, SQLException {
        MedicalRecord record = new MedicalRecord();
        record.setDoctorID(doctor.getDoctorID());
        readClinicalFields(request, record, true);
        Integer appointmentID;
        try {
            appointmentID = parseOptionalID(request.getParameter("appointmentID"));
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        record.setAppointmentID(appointmentID);
        Appointment appointment = null;
        if (appointmentID != null) {
            appointment = new AppointmentDAO(getServletContext())
                    .getDetailsForDoctor(appointmentID, doctor.getDoctorID());
            if (appointment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            record.setPatientID(appointment.getPatientID());
        } else {
            try {
                record.setPatientID(parseID(request.getParameter("patientID")));
            } catch (NumberFormatException ex) {
                renderNewForm(request, response, doctor, null, record,
                        "Search for and select a patient first.");
                return;
            }
        }

        String error = validateClinicalFields(record, request);
        if (error == null) {
            error = readItems(request, record);
        }
        if (error != null) {
            renderNewForm(request, response, doctor, appointment, record, error);
            return;
        }

        boolean createInvoice = "true".equals(request.getParameter("createInvoice"));
        try {
            MedicalRecordDAO dao = new MedicalRecordDAO(getServletContext());
            if (appointmentID != null) {
                Integer existingID = dao.getIDByAppointmentForDoctor(appointmentID, doctor.getDoctorID());
                if (existingID != null) {
                    response.sendRedirect(request.getContextPath()
                            + "/doctor/medical-records?id=" + existingID);
                    return;
                }
            }
            if (!dao.saveForDoctor(record, createInvoice, true)) {
                response.sendError(HttpServletResponse.SC_CONFLICT);
                return;
            }
            request.getSession(false).setAttribute("doctorMedicalRecordMessage", "Medical record created.");
            response.sendRedirect(request.getContextPath() + "/doctor/medical-records");
        } catch (IllegalArgumentException ex) {
            renderNewForm(request, response, doctor, appointment, record, ex.getMessage());
        }
    }

    private void updateRecord(HttpServletRequest request, HttpServletResponse response, Doctor doctor)
            throws ServletException, IOException, SQLException {
        int recordID;
        try {
            recordID = parseID(request.getParameter("id"));
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        MedicalRecord record = new MedicalRecordDAO(getServletContext())
                .getDetailsForDoctor(recordID, doctor.getDoctorID());
        if (record == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        readClinicalFields(request, record, false);
        String error = validateClinicalFields(record, request);
        boolean itemsEditable = itemsEditable(record);
        if (error == null && itemsEditable) {
            error = readItems(request, record);
        }
        if (error != null) {
            renderEditForm(request, response, doctor, record, error);
            return;
        }

        boolean createInvoice = record.getInvoice() == null
                && "true".equals(request.getParameter("createInvoice"));
        try {
            MedicalRecordDAO saveDAO = new MedicalRecordDAO(getServletContext());
            if (!saveDAO.saveForDoctor(record, createInvoice, itemsEditable)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.getSession(false).setAttribute("doctorMedicalRecordMessage",
                    "Medical record changes saved.");
            response.sendRedirect(request.getContextPath() + "/doctor/medical-records?id=" + recordID);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            renderEditForm(request, response, doctor, record, ex.getMessage());
        }
    }

    private void readClinicalFields(HttpServletRequest request, MedicalRecord record, boolean creating) {
        record.setSymptoms(blankToNull(request.getParameter("symptoms")));
        record.setDiagnosis(blankToNull(request.getParameter("diagnosis")));
        record.setTreatmentNotes(blankToNull(request.getParameter("treatmentNotes")));
        String status = value(request.getParameter("status")).trim();
        record.setStatus(status.isEmpty() && creating ? "IN_PROGRESS" : status);
    }

    private String validateClinicalFields(MedicalRecord record, HttpServletRequest request) {
        if (!RECORD_STATUSES.contains(record.getStatus())) {
            return "Select a valid medical record status.";
        }
        if (length(record.getSymptoms()) > 1000) {
            return "Symptoms must be at most 1,000 characters.";
        }
        if (length(record.getDiagnosis()) > 1000) {
            return "Diagnosis must be at most 1,000 characters.";
        }
        if (length(record.getTreatmentNotes()) > 2000) {
            return "Treatment notes must be at most 2,000 characters.";
        }
        String notes = value(request.getParameter("prescriptionNotes")).trim();
        if (notes.length() > 1000) {
            return "Prescription notes must be at most 1,000 characters.";
        }
        if (request.getParameter("createInvoice") != null
                && !"true".equals(request.getParameter("createInvoice"))) {
            return "Invalid invoice option.";
        }
        return null;
    }

    private String readItems(HttpServletRequest request, MedicalRecord record) {
        String error = readServices(request, record);
        if (error != null) {
            return error;
        }
        List<MedicineLine> medicines = new ArrayList<>();
        String[] ids = request.getParameterValues("medicineID");
        String[] quantities = request.getParameterValues("medicineQuantity");
        String[] dosages = request.getParameterValues("dosage");
        String[] frequencies = request.getParameterValues("frequency");
        String[] durations = request.getParameterValues("durationDays");
        String[] instructions = request.getParameterValues("instructions");
        int count = ids == null ? 0 : ids.length;
        if (count > 50 || !sameLength(count, quantities, dosages, frequencies, durations, instructions)) {
            return "Medicine selections are invalid. Search and add the medicines again.";
        }
        for (int i = 0; i < count; i++) {
            try {
                MedicineLine line = new MedicineLine();
                line.setMedicineID(parseID(ids[i]));
                line.setQuantity(parseID(quantities[i]));
                line.setDosage(blankToNull(dosages[i]));
                line.setFrequency(blankToNull(frequencies[i]));
                line.setDurationDays(durations[i] == null || durations[i].isBlank()
                        ? null : parseID(durations[i]));
                line.setInstructions(blankToNull(instructions[i]));
                if (length(line.getDosage()) > 100 || length(line.getFrequency()) > 200
                        || length(line.getInstructions()) > 500) {
                    return "Medicine directions exceed the allowed field lengths.";
                }
                medicines.add(line);
            } catch (NumberFormatException ex) {
                return "Medicine IDs, quantities, and duration must be valid positive numbers.";
            }
        }
        Prescription prescription = new Prescription();
        prescription.setNotes(blankToNull(request.getParameter("prescriptionNotes")));
        prescription.getMedicines().addAll(medicines);
        record.getPrescriptions().clear();
        if (!medicines.isEmpty() || prescription.getNotes() != null) {
            record.getPrescriptions().add(prescription);
        }
        return null;
    }

    private String readServices(HttpServletRequest request, MedicalRecord record) {
        String[] ids = request.getParameterValues("serviceID");
        String[] quantities = request.getParameterValues("serviceQuantity");
        String[] notes = request.getParameterValues("serviceNotes");
        int count = ids == null ? 0 : ids.length;
        if (count > 50 || !sameLength(count, quantities, notes)) {
            return "Service selections are invalid. Search and add the services again.";
        }
        List<ServiceLine> services = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                ServiceLine line = new ServiceLine();
                line.setServiceID(parseID(ids[i]));
                line.setQuantity(parseID(quantities[i]));
                line.setNotes(blankToNull(notes[i]));
                if (length(line.getNotes()) > 500) {
                    return "Service notes must be at most 500 characters.";
                }
                services.add(line);
            } catch (NumberFormatException ex) {
                return "Service IDs and quantities must be valid positive numbers.";
            }
        }
        record.getServices().clear();
        record.getServices().addAll(services);
        return null;
    }

    private boolean sameLength(int count, String[]... values) {
        for (String[] value : values) {
            if ((count == 0 && value != null && value.length != 0)
                    || (count > 0 && (value == null || value.length != count))) {
                return false;
            }
        }
        return true;
    }

    private boolean itemsEditable(MedicalRecord record) {
        if (record.getInvoice() != null) {
            return false;
        }
        for (Prescription prescription : record.getPrescriptions()) {
            if ("ISSUED".equals(prescription.getStatus())
                    || "CANCELLED".equals(prescription.getStatus())) {
                return false;
            }
        }
        return true;
    }

    private void loadNewForm(
            HttpServletRequest request, HttpServletResponse response, Doctor doctor, String error)
            throws ServletException, IOException, SQLException {
        Integer appointmentID;
        try {
            appointmentID = parseOptionalID(request.getParameter("appointmentID"));
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Appointment appointment = null;
        if (appointmentID != null) {
            Integer existingID = new MedicalRecordDAO(getServletContext())
                    .getIDByAppointmentForDoctor(appointmentID, doctor.getDoctorID());
            if (existingID != null) {
                response.sendRedirect(request.getContextPath()
                        + "/doctor/medical-records?id=" + existingID);
                return;
            }
            appointment = new AppointmentDAO(getServletContext())
                    .getDetailsForDoctor(appointmentID, doctor.getDoctorID());
            if (appointment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        MedicalRecord record = new MedicalRecord();
        record.setStatus("IN_PROGRESS");
        renderNewForm(request, response, doctor, appointment, record, error);
    }

    private void renderNewForm(HttpServletRequest request, HttpServletResponse response,
            Doctor doctor, Appointment appointment, MedicalRecord record, String error)
            throws ServletException, IOException {
        if (appointment != null) {
            record.setAppointmentID(appointment.getAppointmentID());
            record.setPatientID(appointment.getPatientID());
            record.setPatientName(appointment.getPatient().getFullName());
        }
        request.setAttribute("record", record);
        request.setAttribute("appointment", appointment);
        request.setAttribute("createMode", true);
        request.setAttribute("doctor", doctor);
        request.setAttribute("itemsEditable", true);
        request.setAttribute("error", error);
        request.setAttribute("patientIDValue", value(request.getParameter("patientID")));
        request.setAttribute("patientNameValue", value(request.getParameter("patientName")));
        request.setAttribute("patientPhoneValue", value(request.getParameter("patientPhone")));
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    private void renderEditForm(HttpServletRequest request, HttpServletResponse response,
            Doctor doctor, MedicalRecord record, String error) throws ServletException, IOException {
        request.setAttribute("record", record);
        request.setAttribute("createMode", false);
        request.setAttribute("doctor", doctor);
        request.setAttribute("itemsEditable", itemsEditable(record));
        request.setAttribute("error", error);
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    private Doctor loadSignedInDoctor(HttpServletRequest request, int doctorID) throws SQLException {
        Doctor doctor = new DoctorDAO(getServletContext()).getByID(doctorID);
        Object userID = request.getSession(false).getAttribute("UserID");
        if (doctor == null || !(userID instanceof Integer)
                || !userID.equals(doctor.getUserID())) {
            throw new IllegalArgumentException("The signed-in Doctor profile could not be verified.");
        }
        return doctor;
    }

    private void searchPatients(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String term = value(request.getParameter("term")).trim();
        if (term.length() > 100) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            List<Patient> patients = new PatientDAO(getServletContext()).searchByNameOrPhone(term);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write("[");
            for (int i = 0; i < patients.size(); i++) {
                Patient patient = patients.get(i);
                if (i > 0) {
                    writer.write(",");
                }
                writer.write("{\"id\":");
                writer.write(Integer.toString(patient.getPatientID()));
                writer.write(",\"name\":\"");
                writer.write(jsonEscape(patient.getFullName()));
                writer.write("\",\"phone\":\"");
                writer.write(jsonEscape(patient.getPhone()));
                writer.write("\"}");
            }
            writer.write("]");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to search patients for Doctor medical record.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Patient search is temporarily unavailable.");
        }
    }

    private void searchServices(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String term = value(request.getParameter("term")).trim();
        if (term.length() > 100) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            List<Service> services = new ServiceDAO(getServletContext()).searchActiveByName(term);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write("[");
            for (int i = 0; i < services.size(); i++) {
                Service service = services.get(i);
                if (i > 0) {
                    writer.write(",");
                }
                writer.write("{\"id\":");
                writer.write(Integer.toString(service.getServiceID()));
                writer.write(",\"name\":\"");
                writer.write(jsonEscape(service.getServiceName()));
                writer.write("\",\"price\":\"");
                writer.write(jsonEscape(service.getPrice().toPlainString()));
                writer.write("\"}");
            }
            writer.write("]");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to search services for Doctor medical record.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Service search is temporarily unavailable.");
        }
    }

    private void searchMedicines(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String term = value(request.getParameter("term")).trim();
        if (term.length() > 100) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            List<Medicine> medicines = new MedicineDAO(getServletContext()).searchActiveByName(term);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write("[");
            for (int i = 0; i < medicines.size(); i++) {
                Medicine medicine = medicines.get(i);
                if (i > 0) {
                    writer.write(",");
                }
                writer.write("{\"id\":");
                writer.write(Integer.toString(medicine.getMedicineID()));
                writer.write(",\"name\":\"");
                writer.write(jsonEscape(medicine.getMedicineName()));
                writer.write("\",\"ingredient\":\"");
                writer.write(jsonEscape(value(medicine.getActiveIngredient())));
                writer.write("\",\"unit\":\"");
                writer.write(jsonEscape(medicine.getUnit()));
                writer.write("\",\"price\":\"");
                writer.write(jsonEscape(medicine.getPrice().toPlainString()));
                writer.write("\",\"stock\":");
                writer.write(Integer.toString(medicine.getStockQuantity()));
                writer.write("}");
            }
            writer.write("]");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to search medicines for Doctor medical record.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Medicine search is temporarily unavailable.");
        }
    }

    private Integer requireDoctor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("UserID") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        if (!"DOCTOR".equals(session.getAttribute("Role"))
                || !(session.getAttribute("DoctorID") instanceof Integer doctorID) || doctorID <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return doctorID;
    }

    private int parseID(String value) {
        int id = Integer.parseInt(value);
        if (id <= 0) {
            throw new NumberFormatException("ID must be positive.");
        }
        return id;
    }

    private Integer parseOptionalID(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseID(value);
    }

    private int length(String value) {
        return value == null ? 0 : value.length();
    }

    private String blankToNull(String input) {
        String trimmed = value(input).trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String value(String input) {
        return input == null ? "" : input;
    }

    private String jsonEscape(String input) {
        StringBuilder escaped = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                case '<' -> escaped.append("\\u003c");
                case '>' -> escaped.append("\\u003e");
                case '&' -> escaped.append("\\u0026");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
