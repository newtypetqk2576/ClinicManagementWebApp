package Controller;

import Dal.UserDAO;
import Dal.UserDAO.DuplicateRegistrationException;
import Model.Patient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/register")
public class PatientRegistrationServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientRegistrationServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
        request.getRequestDispatcher("/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = value(request.getParameter("username")).trim();
        String password = value(request.getParameter("password"));
        String confirmPassword = value(request.getParameter("confirmPassword"));
        String fullName = value(request.getParameter("fullName")).trim();
        String gender = value(request.getParameter("gender"));
        String phone = value(request.getParameter("phone")).trim();
        String email = blankToNull(request.getParameter("email"));
        String address = blankToNull(request.getParameter("address"));

        String validationError = validate(username, password, confirmPassword,
                fullName, gender, phone, email, address);
        if (validationError != null) {
            forwardError(request, response, validationError);
            return;
        }

        try {
            new UserDAO(getServletContext()).registerPatient(
                    username, password, fullName, gender, phone, email, address);
            response.sendRedirect(request.getContextPath() + "/login?registered=1");
        } catch (DuplicateRegistrationException ex) {
            String message = switch (ex.getField()) {
                case "username" -> "That username is already in use.";
                case "phone" -> "That phone number is already registered.";
                default -> "That username or phone number is already in use.";
            };
            forwardError(request, response, message);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to register patient account.", ex);
            forwardError(request, response,
                    "Registration is temporarily unavailable. Please try again.");
        }
    }

    private String validate(String username, String password, String confirmPassword,
            String fullName, String gender, String phone, String email, String address) {
        if (username.isEmpty() || username.length() > 50) {
            return "Username is required and must be at most 50 characters.";
        }
        if (password.isEmpty() || password.length() > 255) {
            return "Password is required and must be at most 255 characters.";
        }
        if (!password.equals(confirmPassword)) {
            return "The passwords do not match.";
        }
        if (fullName.isEmpty() || fullName.length() > 100) {
            return "Full name is required and must be at most 100 characters.";
        }
        if (!Patient.isValidGender(gender)) {
            return "Select a valid gender.";
        }
        if (phone.isEmpty() || phone.length() > 15) {
            return "Phone is required and must be at most 15 characters.";
        }
        if (email != null && email.length() > 100) {
            return "Email must be at most 100 characters.";
        }
        if (address != null && address.length() > 255) {
            return "Address must be at most 255 characters.";
        }
        return null;
    }

    private void forwardError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("genderOptions", Patient.GENDER_OPTIONS);
        request.setAttribute("gender", value(request.getParameter("gender")));
        request.setAttribute("error", error);
        request.getRequestDispatcher("/auth/register.jsp").forward(request, response);
    }

    private String blankToNull(String input) {
        String value = value(input).trim();
        return value.isEmpty() ? null : value;
    }

    private String value(String input) {
        return input == null ? "" : input;
    }
}
