package Controller;

import Dal.AppointmentDAO;
import Dal.PatientDAO;
import Model.Appointment;
import Model.Patient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {
    "/doctor/appointments",
    "/doctor/appointments/new",
    "/doctor/patients/search"
})
public class DoctorAppointmentsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorAppointmentsServlet.class.getName());
    private static final String LIST_PAGE = "/view/doctor-appointments.jsp";
    private static final String DETAIL_PAGE = "/view/doctor-appointment-detail.jsp";
    private static final String CREATE_PAGE = "/view/doctor-appointment-form.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer doctorID = requireDoctor(request, response);
        if (doctorID == null) {
            return;
        }
        String path = request.getServletPath();
        if ("/doctor/patients/search".equals(path)) {
            searchPatients(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        request.setAttribute("message", session.getAttribute("doctorAppointmentMessage"));
        request.setAttribute("error", session.getAttribute("doctorAppointmentError"));
        session.removeAttribute("doctorAppointmentMessage");
        session.removeAttribute("doctorAppointmentError");
        request.setAttribute("doctorID", doctorID);

        if ("/doctor/appointments/new".equals(path)) {
            renderCreateForm(request, response, null);
            return;
        }

        String idValue = request.getParameter("id");
        try {
            AppointmentDAO appointmentDAO = new AppointmentDAO(getServletContext());
            if (idValue == null) {
                List<Appointment> appointments = appointmentDAO.getAllForDoctor(doctorID);
                request.setAttribute("appointments", appointments);
                request.getRequestDispatcher(LIST_PAGE).forward(request, response);
                return;
            }

            int appointmentID = parseID(idValue);
            Appointment appointment = appointmentDAO.getDetailsForDoctor(appointmentID, doctorID);
            if (appointment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("appointment", appointment);
            request.getRequestDispatcher(DETAIL_PAGE).forward(request, response);
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load doctor appointments.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Appointments are temporarily unavailable.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Integer doctorID = requireDoctor(request, response);
        if (doctorID == null) {
            return;
        }
        String action = request.getParameter("action");
        if ("create".equals(action)) {
            create(request, response, doctorID);
            return;
        }
        if (!"confirm".equals(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int appointmentID;
        try {
            appointmentID = parseID(request.getParameter("id"));
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        HttpSession session = request.getSession(false);
        try {
            if (new AppointmentDAO(getServletContext()).confirmForDoctor(appointmentID, doctorID)) {
                session.setAttribute("doctorAppointmentMessage", "Appointment confirmed.");
            } else {
                session.setAttribute("doctorAppointmentError",
                        "This appointment is not assigned to you or is no longer BOOKED.");
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to confirm doctor appointment.", ex);
            session.setAttribute("doctorAppointmentError",
                    "Appointment confirmation is temporarily unavailable. Please try again.");
        }
        response.sendRedirect(request.getContextPath() + "/doctor/appointments?id=" + appointmentID);
    }

    private void create(HttpServletRequest request, HttpServletResponse response, int doctorID)
            throws ServletException, IOException {
        String patientIDValue = value(request.getParameter("patientID")).trim();
        String appointmentDateValue = value(request.getParameter("appointmentDate")).trim();
        String reason = value(request.getParameter("reason")).trim();
        request.setAttribute("patientIDValue", patientIDValue);
        request.setAttribute("appointmentDateValue", appointmentDateValue);
        request.setAttribute("reasonValue", reason);

        int patientID;
        try {
            patientID = parseID(patientIDValue);
        } catch (NumberFormatException ex) {
            renderCreateForm(request, response, "Select a patient from the search results.");
            return;
        }

        LocalDateTime appointmentDate;
        try {
            appointmentDate = LocalDateTime.parse(
                    appointmentDateValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            renderCreateForm(request, response, "Enter a valid appointment date and time.");
            return;
        }
        if (!appointmentDate.isAfter(LocalDateTime.now())) {
            renderCreateForm(request, response, "Appointment date and time must be in the future.");
            return;
        }
        if (reason.length() > 255) {
            renderCreateForm(request, response, "Reason must be at most 255 characters.");
            return;
        }

        try {
            PatientDAO patientDAO = new PatientDAO(getServletContext());
            if (!patientDAO.existsByID(patientID)) {
                renderCreateForm(request, response, "The selected patient could not be found. Search again.");
                return;
            }
            if (!new AppointmentDAO(getServletContext()).createForPatient(
                    patientID, doctorID, appointmentDate, reason.isEmpty() ? null : reason)) {
                renderCreateForm(request, response, "This doctor account is no longer active.");
                return;
            }
            request.getSession(false).setAttribute("doctorAppointmentMessage", "Appointment created.");
            response.sendRedirect(request.getContextPath() + "/doctor/appointments");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to create doctor appointment.", ex);
            renderCreateForm(request, response,
                    "Appointment creation is temporarily unavailable. Please try again.");
        }
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
            response.setHeader("Cache-Control", "no-store, private");
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
            LOGGER.log(Level.SEVERE, "Unable to search patients for doctor appointment.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Patient search is temporarily unavailable.");
        }
    }

    private void renderCreateForm(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        request.getRequestDispatcher(CREATE_PAGE).forward(request, response);
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

    private Integer requireDoctor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("UserID") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        if (!"DOCTOR".equals(session.getAttribute("Role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        Object value = session.getAttribute("DoctorID");
        if (!(value instanceof Integer doctorID) || doctorID <= 0) {
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

    private String value(String input) {
        return input == null ? "" : input;
    }
}
