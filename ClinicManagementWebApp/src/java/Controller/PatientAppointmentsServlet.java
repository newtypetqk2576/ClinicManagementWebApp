package Controller;

import Dal.AppointmentDAO;
import Dal.AppointmentDAO.CreationResult;
import Dal.DoctorDAO;
import Model.Appointment;
import Model.Doctor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/appointments")
public class PatientAppointmentsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientAppointmentsServlet.class.getName());
    private static final String PAGE = "/view/appointments.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer patientID = requirePatient(request, response);
        if (patientID == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        request.setAttribute("message", session.getAttribute("appointmentMessage"));
        request.setAttribute("error", session.getAttribute("appointmentError"));
        session.removeAttribute("appointmentMessage");
        session.removeAttribute("appointmentError");
        render(request, response, patientID);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Integer patientID = requirePatient(request, response);
        if (patientID == null) {
            return;
        }

        String action = request.getParameter("action");
        if ("create".equals(action)) {
            create(request, response, patientID);
        } else if ("cancel".equals(action)) {
            cancel(request, response, patientID);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response, int patientID)
            throws ServletException, IOException {
        String doctorValue = value(request.getParameter("doctorID"));
        String dateValue = value(request.getParameter("appointmentDate"));
        String reason = value(request.getParameter("reason")).trim();
        request.setAttribute("doctorIDValue", doctorValue);
        request.setAttribute("appointmentDateValue", dateValue);
        request.setAttribute("reasonValue", reason);

        int doctorID;
        LocalDateTime appointmentDate;
        try {
            doctorID = Integer.parseInt(doctorValue);
            if (doctorID <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            renderError(request, response, patientID, "Select an active doctor.");
            return;
        }
        try {
            appointmentDate = LocalDateTime.parse(dateValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            renderError(request, response, patientID, "Enter a valid appointment date and time.");
            return;
        }
        if (!appointmentDate.isAfter(LocalDateTime.now())) {
            renderError(request, response, patientID, "Appointment date and time must be in the future.");
            return;
        }
        if (reason.length() > 255) {
            renderError(request, response, patientID, "Reason must be at most 255 characters.");
            return;
        }

        try {
            CreationResult result = new AppointmentDAO(getServletContext()).createForPatient(
                    patientID, doctorID, appointmentDate, reason.isEmpty() ? null : reason);
            if (result != CreationResult.CREATED) {
                String error = switch (result) {
                    case DOCTOR_BUSY -> "Bác sĩ đã có lịch hẹn vào thời điểm này. Vui lòng chọn giờ hoặc bác sĩ khác.";
                    case PATIENT_BUSY -> "Bạn đã có lịch hẹn vào thời điểm này. Vui lòng chọn giờ khác.";
                    case DOCTOR_UNAVAILABLE -> "Bác sĩ đã ngừng nhận lịch. Vui lòng chọn bác sĩ khác.";
                    default -> throw new IllegalStateException("Unexpected appointment result.");
                };
                renderError(request, response, patientID, error);
                return;
            }
            request.getSession(false).setAttribute("appointmentMessage", "Appointment created.");
            response.sendRedirect(request.getContextPath() + "/appointments");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to create patient appointment.", ex);
            renderError(request, response, patientID,
                    "Appointment creation is temporarily unavailable. Please try again.");
        }
    }

    private void cancel(HttpServletRequest request, HttpServletResponse response, int patientID)
            throws IOException {
        int appointmentID;
        try {
            appointmentID = Integer.parseInt(value(request.getParameter("appointmentID")));
            if (appointmentID <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            HttpSession session = request.getSession(false);
            if (new AppointmentDAO(getServletContext()).cancelForPatient(appointmentID, patientID)) {
                session.setAttribute("appointmentMessage", "Appointment cancelled.");
            } else {
                session.setAttribute("appointmentError",
                        "This appointment cannot be cancelled.");
            }
            response.sendRedirect(request.getContextPath() + "/appointments");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to cancel patient appointment.", ex);
            request.getSession(false).setAttribute("appointmentError",
                    "Appointment cancellation is temporarily unavailable. Please try again.");
            response.sendRedirect(request.getContextPath() + "/appointments");
        }
    }

    private Integer requirePatient(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("UserID") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        if (!"PATIENT".equals(session.getAttribute("Role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        Object patientID = session.getAttribute("PatientID");
        if (!(patientID instanceof Integer id) || id <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return id;
    }

    private void renderError(HttpServletRequest request, HttpServletResponse response,
            int patientID, String error) throws ServletException, IOException {
        request.setAttribute("error", error);
        render(request, response, patientID);
    }

    private void render(HttpServletRequest request, HttpServletResponse response, int patientID)
            throws ServletException, IOException {
        try {
            List<Doctor> activeDoctors = new DoctorDAO(getServletContext()).getActive();
            request.setAttribute("doctors", activeDoctors);
            ListPage.byNameAndDate(request, "appointments",
                    new AppointmentDAO(getServletContext()).getByPatientID(patientID),
                    "Tên bác sĩ", Appointment::getDoctorName, "Ngày hẹn", Appointment::getAppointmentDate);
            request.getRequestDispatcher(PAGE).forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load patient appointments.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Appointments are temporarily unavailable.");
        }
    }

    private String value(String input) {
        return input == null ? "" : input;
    }
}
