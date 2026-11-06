package Controller;

import Dal.DoctorDAO;
import Model.Doctor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/admin/doctors", "/admin/doctors/new", "/admin/doctors/edit"})
public class ManagerDoctorsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ManagerDoctorsServlet.class.getName());
    private static final String LIST_PAGE = "/view/manager-doctors.jsp";
    private static final String FORM_PAGE = "/view/manager-doctor-form.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        if (!requireManager(request, response)) {
            return;
        }

        String path = request.getServletPath();
        if ("/admin/doctors/new".equals(path)) {
            Doctor doctor = new Doctor();
            doctor.setActive(true);
            renderForm(request, response, doctor, true, "true", null);
        } else if ("/admin/doctors/edit".equals(path)) {
            try {
                Doctor doctor = new DoctorDAO(getServletContext()).getByID(parseID(request.getParameter("id")));
                if (doctor == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                renderForm(request, response, doctor, false, Boolean.toString(doctor.isActive()), null);
            } catch (NumberFormatException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Unable to load manager doctor.", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Doctor information is temporarily unavailable.");
            }
        } else {
            renderList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        request.setCharacterEncoding("UTF-8");
        if (!requireManager(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if ("create".equals(action)) {
            save(request, response, true);
        } else if ("update".equals(action)) {
            save(request, response, false);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void save(HttpServletRequest request, HttpServletResponse response, boolean creating)
            throws ServletException, IOException {
        Doctor doctor;
        if (creating) {
            doctor = new Doctor();
        } else {
            try {
                doctor = new DoctorDAO(getServletContext()).getByID(parseID(request.getParameter("id")));
                if (doctor == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } catch (NumberFormatException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Unable to load doctor for update.", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Doctor information is temporarily unavailable.");
                return;
            }
        }

        doctor.setFullName(value(request.getParameter("fullName")).trim());
        doctor.setSpecialty(value(request.getParameter("specialty")).trim());
        doctor.setPhone(blankToNull(request.getParameter("phone")));
        String activeValue = request.getParameter("isActive");
        if ("true".equals(activeValue)) {
            doctor.setActive(true);
        } else if ("false".equals(activeValue)) {
            doctor.setActive(false);
        }
        String error = validate(doctor, activeValue);
        if (error != null) {
            renderForm(request, response, doctor, creating, activeValue, error);
            return;
        }

        try {
            DoctorDAO dao = new DoctorDAO(getServletContext());
            if (creating) {
                dao.create(doctor);
            } else if (!dao.update(doctor)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.getSession(false).setAttribute("managerDoctorMessage",
                    creating ? "Doctor created." : "Doctor information saved.");
            response.sendRedirect(request.getContextPath() + "/admin/doctors");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to save manager doctor.", ex);
            renderForm(request, response, doctor, creating, activeValue,
                    "Doctor could not be saved right now. Please try again.");
        }
    }

    private String validate(Doctor doctor, String activeValue) {
        if (doctor.getFullName().isEmpty() || doctor.getFullName().length() > 100) {
            return "Full name is required and must be at most 100 characters.";
        }
        if (doctor.getSpecialty().isEmpty() || doctor.getSpecialty().length() > 100) {
            return "Specialty is required and must be at most 100 characters.";
        }
        if (doctor.getPhone() != null && doctor.getPhone().length() > 15) {
            return "Phone must be at most 15 characters.";
        }
        if (!"true".equals(activeValue) && !"false".equals(activeValue)) {
            return "Select a valid doctor status.";
        }
        return null;
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        request.setAttribute("message", session.getAttribute("managerDoctorMessage"));
        session.removeAttribute("managerDoctorMessage");
        try {
            List<Doctor> doctors = new DoctorDAO(getServletContext()).getAll();
            ListPage.byName(request, "doctors", doctors,
                    "Tên bác sĩ", Doctor::getFullName);
            request.getRequestDispatcher(LIST_PAGE).forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load manager doctor list.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Doctor list is temporarily unavailable.");
        }
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response, Doctor doctor,
            boolean creating, String activeValue, String error) throws ServletException, IOException {
        request.setAttribute("doctor", doctor);
        request.setAttribute("createMode", creating);
        request.setAttribute("activeValue", activeValue);
        request.setAttribute("error", error);
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    private boolean requireManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("UserID") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!"MANAGER".equals(session.getAttribute("Role"))) {
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
