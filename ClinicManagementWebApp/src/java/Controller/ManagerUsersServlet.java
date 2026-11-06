package Controller;

import Dal.UserDAO;
import Dal.UserDAO.DuplicateRegistrationException;
import Dal.UserDAO.UnlinkedProfileUnavailableException;
import Model.Doctor;
import Model.Patient;
import Model.User;
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

@WebServlet(urlPatterns = {
    "/admin/users",
    "/admin/users/new",
    "/admin/users/detail",
    "/admin/users/search-patients",
    "/admin/users/search-doctors"
})
public class ManagerUsersServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ManagerUsersServlet.class.getName());
    private static final String LIST_PAGE = "/view/manager-users.jsp";
    private static final String FORM_PAGE = "/view/manager-user-form.jsp";
    private static final String DETAIL_PAGE = "/view/manager-user-detail.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        if (!requireManager(request, response)) {
            return;
        }

        String path = request.getServletPath();
        if ("/admin/users/search-patients".equals(path)) {
            searchPatients(request, response);
        } else if ("/admin/users/search-doctors".equals(path)) {
            searchDoctors(request, response);
        } else if ("/admin/users/new".equals(path)) {
            renderCreateForm(request, response, null, "");
        } else if ("/admin/users/detail".equals(path)) {
            renderDetails(request, response);
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
            createUser(request, response);
        } else if ("update".equals(action)) {
            updateUser(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void createUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = value(request.getParameter("username")).trim();
        String password = value(request.getParameter("password"));
        Integer patientID;
        Integer doctorID;
        try {
            patientID = parseOptionalID(request.getParameter("patientID"));
            doctorID = parseOptionalID(request.getParameter("doctorID"));
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String role = value(request.getParameter("role"));
        String error = validateNewUser(username, password, role, patientID, doctorID);
        if (error != null) {
            renderCreateForm(request, response, error, username, role, patientID, doctorID);
            return;
        }

        try {
            new UserDAO(getServletContext()).createUserForManager(
                    username, password, role, patientID, doctorID);
            request.getSession(false).setAttribute("managerUserMessage", "User account created.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
        } catch (DuplicateRegistrationException ex) {
            renderCreateForm(request, response, "That username is already in use.",
                    username, role, patientID, doctorID);
        } catch (UnlinkedProfileUnavailableException ex) {
            renderCreateForm(request, response,
                    "The selected patient or doctor already has an account. Search and select another profile.",
                    username, role, patientID, doctorID);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to create manager user account.", ex);
            renderCreateForm(request, response,
                    "The user account could not be created right now. Please try again.",
                    username, role, patientID, doctorID);
        }
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int userID;
        try {
            userID = parseID(request.getParameter("id"));
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String username = value(request.getParameter("username")).trim();
        String passwordInput = value(request.getParameter("password"));
        String password = passwordInput.isEmpty() ? null : passwordInput;
        String activeValue = request.getParameter("isActive");
        String error = validateUpdatedUser(username, password, activeValue);
        if (error != null) {
            renderDetails(request, response, userID, error);
            return;
        }

        try {
            if (!new UserDAO(getServletContext()).updateUserForManager(
                    userID, username, password, "true".equals(activeValue))) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.getSession(false).setAttribute("managerUserMessage", "User information saved.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
        } catch (DuplicateRegistrationException ex) {
            renderDetails(request, response, userID, "That username is already in use.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to update manager user account.", ex);
            renderDetails(request, response, userID,
                    "The user account could not be updated right now. Please try again.");
        }
    }

    private String validateNewUser(String username, String password, String role,
            Integer patientID, Integer doctorID) {
        if (username.isEmpty() || username.length() > 50) {
            return "Username is required and must be at most 50 characters.";
        }
        if (password.isEmpty() || password.length() > 255) {
            return "Password is required and must be at most 255 characters.";
        }
        if ("MANAGER".equals(role) && patientID == null && doctorID == null) {
            return null;
        }
        if ("PATIENT".equals(role) && patientID != null && doctorID == null) {
            return null;
        }
        if ("DOCTOR".equals(role) && doctorID != null && patientID == null) {
            return null;
        }
        return "Select a valid role and, for patient or doctor accounts, exactly one matching profile.";
    }

    private String validateUpdatedUser(String username, String password, String activeValue) {
        if (username.isEmpty() || username.length() > 50) {
            return "Username is required and must be at most 50 characters.";
        }
        if (password != null && password.length() > 255) {
            return "Password must be at most 255 characters.";
        }
        if (!"true".equals(activeValue) && !"false".equals(activeValue)) {
            return "Select a valid account status.";
        }
        return null;
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        request.setAttribute("message", session.getAttribute("managerUserMessage"));
        session.removeAttribute("managerUserMessage");
        try {
            List<User> users = new UserDAO(getServletContext()).getUsersForManager();
            ListPage.byNameAndDate(request, "users", users,
                    "Tên đăng nhập", User::getUsername,
                    "Ngày tạo", User::getCreatedAt);
            request.getRequestDispatcher(LIST_PAGE).forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load manager user list.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "User list is temporarily unavailable.");
        }
    }

    private void renderCreateForm(HttpServletRequest request, HttpServletResponse response,
            String error, String username) throws ServletException, IOException {
        renderCreateForm(request, response, error, username,
                value(request.getParameter("role")),
                parseFormID(request.getParameter("patientID")),
                parseFormID(request.getParameter("doctorID")));
    }

    private void renderCreateForm(HttpServletRequest request, HttpServletResponse response,
            String error, String username, String role, Integer patientID, Integer doctorID)
            throws ServletException, IOException {
        request.setAttribute("patientSearch", value(request.getParameter("patientSearch")));
        request.setAttribute("doctorSearch", value(request.getParameter("doctorSearch")));
        request.setAttribute("role", role);
        request.setAttribute("patientID", patientID);
        request.setAttribute("doctorID", doctorID);
        request.setAttribute("error", error);
        request.setAttribute("username", username);
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    private void searchPatients(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String term = value(request.getParameter("term")).trim();
        if (term.length() > 100) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            List<Patient> patients = new UserDAO(getServletContext())
                    .searchUnlinkedPatientsForManager(term);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store, private");
            response.getWriter().write("[");
            for (int i = 0; i < patients.size(); i++) {
                Patient patient = patients.get(i);
                if (i > 0) {
                    response.getWriter().write(",");
                }
                response.getWriter().write("{\"id\":");
                response.getWriter().write(Integer.toString(patient.getPatientID()));
                response.getWriter().write(",\"name\":\"");
                response.getWriter().write(jsonEscape(patient.getFullName()));
                response.getWriter().write("\",\"phone\":\"");
                response.getWriter().write(jsonEscape(value(patient.getPhone())));
                response.getWriter().write("\"}");
            }
            response.getWriter().write("]");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to search unlinked patients for manager user.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Patient search is temporarily unavailable.");
        }
    }

    private void searchDoctors(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String term = value(request.getParameter("term")).trim();
        if (term.length() > 100) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            List<Doctor> doctors = new UserDAO(getServletContext())
                    .searchUnlinkedDoctorsForManager(term);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store, private");
            response.getWriter().write("[");
            for (int i = 0; i < doctors.size(); i++) {
                Doctor doctor = doctors.get(i);
                if (i > 0) {
                    response.getWriter().write(",");
                }
                response.getWriter().write("{\"id\":");
                response.getWriter().write(Integer.toString(doctor.getDoctorID()));
                response.getWriter().write(",\"name\":\"");
                response.getWriter().write(jsonEscape(doctor.getFullName()));
                response.getWriter().write("\",\"phone\":\"");
                response.getWriter().write(jsonEscape(value(doctor.getPhone())));
                response.getWriter().write("\"}");
            }
            response.getWriter().write("]");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to search unlinked doctors for manager user.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Doctor search is temporarily unavailable.");
        }
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

    private Integer parseFormID(String value) {
        try {
            return parseOptionalID(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void renderDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int userID;
        try {
            userID = parseID(request.getParameter("id"));
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        renderDetails(request, response, userID, null);
    }

    private void renderDetails(HttpServletRequest request, HttpServletResponse response,
            int userID, String error) throws ServletException, IOException {
        try {
            User user = new UserDAO(getServletContext()).getUserDetailsForManager(userID);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("user", user);
            request.setAttribute("error", error);
            request.getRequestDispatcher(DETAIL_PAGE).forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load manager user details.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "User information is temporarily unavailable.");
        }
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

    private Integer parseOptionalID(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : parseID(trimmed);
    }

    private String value(String input) {
        return input == null ? "" : input;
    }
}
