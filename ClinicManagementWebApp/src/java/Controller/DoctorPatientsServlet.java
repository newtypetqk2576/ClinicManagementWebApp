package Controller;

import Dal.PatientDAO;
import Dal.PatientDAO.DuplicatePhoneException;
import Model.Patient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/patients")
public class DoctorPatientsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorPatientsServlet.class.getName());
    private static final String LIST_PAGE = "/view/doctor-patients.jsp";
    private static final String EDIT_PAGE = "/view/doctor-patient-edit.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        if (!requireManagerOrDoctor(request, response)) {
            return;
        }
        HttpSession session = request.getSession(false);
        request.setAttribute("message", session.getAttribute("patientMessage"));
        request.setAttribute("error", session.getAttribute("patientError"));
        session.removeAttribute("patientMessage");
        session.removeAttribute("patientError");

        String idValue = request.getParameter("id");
        if (idValue == null) {
            request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
            request.setAttribute("formPatient", new Patient());
            renderList(request, response);
            return;
        }
        try {
            Patient patient = new PatientDAO(getServletContext()).getByID(parseID(idValue));
            if (patient == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("patient", patient);
            request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
            request.setAttribute("dateOfBirthValue",
                    patient.getDateOfBirth() == null ? "" : patient.getDateOfBirth().toString());
            request.getRequestDispatcher(EDIT_PAGE).forward(request, response);
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load doctor patient.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Patient information is temporarily unavailable.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        request.setCharacterEncoding("UTF-8");
        if (!requireManagerOrDoctor(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if ("create".equals(action)) {
            createPatient(request, response);
        } else if ("update".equals(action)) {
            updatePatient(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void createPatient(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Patient patient;
        try {
            patient = readPatient(request, 0);
        } catch (DateTimeParseException ex) {
            request.setAttribute("formPatient", readPatientFields(request));
            request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
            request.setAttribute("dateOfBirthValue", value(request.getParameter("dateOfBirth")));
            request.setAttribute("error", "Enter a valid date of birth.");
            renderList(request, response);
            return;
        }
        request.setAttribute("dateOfBirthValue",
                patient.getDateOfBirth() == null ? "" : patient.getDateOfBirth().toString());
        String validationError = validate(patient);
        if (validationError != null) {
            request.setAttribute("formPatient", patient);
            request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
            request.setAttribute("error", validationError);
            renderList(request, response);
            return;
        }

        try {
            new PatientDAO(getServletContext()).create(patient);
            request.getSession(false).setAttribute("patientMessage", "Patient created.");
            response.sendRedirect(request.getContextPath() + "/patients");
        } catch (DuplicatePhoneException ex) {
            request.setAttribute("formPatient", patient);
            request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
            request.setAttribute("error", "A patient with this phone number already exists.");
            renderList(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to create doctor patient.", ex);
            request.setAttribute("formPatient", patient);
            request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
            request.setAttribute("error", "Patient creation is temporarily unavailable. Please try again.");
            renderList(request, response);
        }
    }

    private void updatePatient(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int patientID;
        try {
            patientID = parseID(request.getParameter("id"));
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Patient patient;
        try {
            patient = readPatient(request, patientID);
        } catch (DateTimeParseException ex) {
            patient = readPatientFields(request);
            patient.setPatientID(patientID);
            request.setAttribute("patient", patient);
            request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
            request.setAttribute("dateOfBirthValue", value(request.getParameter("dateOfBirth")));
            request.setAttribute("error", "Enter a valid date of birth.");
            request.getRequestDispatcher(EDIT_PAGE).forward(request, response);
            return;
        }
        request.setAttribute("dateOfBirthValue",
                patient.getDateOfBirth() == null ? "" : patient.getDateOfBirth().toString());
        String validationError = validate(patient);
        if (validationError != null) {
            request.setAttribute("patient", patient);
            request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
            request.setAttribute("error", validationError);
            request.getRequestDispatcher(EDIT_PAGE).forward(request, response);
            return;
        }

        try {
            if (!new PatientDAO(getServletContext()).update(patient)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.getSession(false).setAttribute("patientMessage", "Patient information saved.");
            response.sendRedirect(request.getContextPath() + "/patients?id=" + patientID);
        } catch (DuplicatePhoneException ex) {
            request.setAttribute("patient", patient);
            request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
            request.setAttribute("error", "A patient with this phone number already exists.");
            request.getRequestDispatcher(EDIT_PAGE).forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to update doctor patient.", ex);
            request.setAttribute("patient", patient);
            request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
            request.setAttribute("error", "Patient update is temporarily unavailable. Please try again.");
            request.getRequestDispatcher(EDIT_PAGE).forward(request, response);
        }
    }

    private Patient readPatient(HttpServletRequest request, int patientID) {
        Patient patient = readPatientFields(request);
        patient.setPatientID(patientID);
        String dateValue = value(request.getParameter("dateOfBirth")).trim();
        patient.setDateOfBirth(dateValue.isEmpty() ? null : LocalDate.parse(dateValue));
        return patient;
    }

    private Patient readPatientFields(HttpServletRequest request) {
        Patient patient = new Patient();
        patient.setFullName(value(request.getParameter("fullName")).trim());
        patient.setGender(blankToNull(request.getParameter("gender")));
        patient.setPhone(value(request.getParameter("phone")).trim());
        patient.setEmail(blankToNull(request.getParameter("email")));
        patient.setAddress(blankToNull(request.getParameter("address")));
        return patient;
    }

    private String validate(Patient patient) {
        if (patient.getFullName().isEmpty() || patient.getFullName().length() > 100) {
            return "Full name is required and must be at most 100 characters.";
        }
        if (!Patient.isValidGender(patient.getGender())) {
            return "Select a valid gender.";
        }
        if (patient.getPhone().isEmpty() || patient.getPhone().length() > 15) {
            return "Phone is required and must be at most 15 characters.";
        }
        if (patient.getEmail() != null && patient.getEmail().length() > 100) {
            return "Email must be at most 100 characters.";
        }
        if (patient.getEmail() != null
                && !patient.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return "Enter a valid email address.";
        }
        if (patient.getAddress() != null && patient.getAddress().length() > 255) {
            return "Address must be at most 255 characters.";
        }
        return null;
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Patient> patients = new PatientDAO(getServletContext()).getAll();
            request.setAttribute("patients", patients);
            request.getRequestDispatcher(LIST_PAGE).forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load doctor patient list.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Patient list is temporarily unavailable.");
        }
    }

    private boolean requireManagerOrDoctor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("UserID") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        Object role = session.getAttribute("Role");
        if ("DOCTOR".equals(role)
                && (!(session.getAttribute("DoctorID") instanceof Integer doctorID) || doctorID <= 0)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        if (!"DOCTOR".equals(role) && !"MANAGER".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private int parseID(String value) {
        int id = Integer.parseInt(value);
        if (id <= 0) {
            throw new NumberFormatException("ID must be positive.");
        }
        return id;
    }

    private String blankToNull(String input) {
        String trimmed = value(input).trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String value(String input) {
        return input == null ? "" : input;
    }
}
