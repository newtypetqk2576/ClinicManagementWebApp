package Controller;

import Dal.UserDAO;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("registered", "1".equals(request.getParameter("registered")));
        request.getRequestDispatcher("/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = value(request.getParameter("username")).trim();
        String password = value(request.getParameter("password"));
        if (username.isEmpty() || password.isEmpty()) {
            request.setAttribute("error", "Enter both username and password.");
            doGet(request, response);
            return;
        }

        try {
            User user = new UserDAO(getServletContext()).login(username, password);
            if (user == null) {
                request.setAttribute("error", "Incorrect username or password.");
                doGet(request, response);
                return;
            }

            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("UserID", user.getUserID());
            session.setAttribute("Username", user.getUsername());
            session.setAttribute("Role", user.getRole());
            if (user.getPatientID() != null) {
                session.setAttribute("PatientID", user.getPatientID());
            }
            if (user.getDoctorID() != null) {
                session.setAttribute("DoctorID", user.getDoctorID());
            }
            String destination = switch (user.getRole()) {
                case "DOCTOR" -> "/patients";
                case "PATIENT" -> "/appointments";
                default -> "/";
            };
            response.sendRedirect(request.getContextPath() + destination);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to process login.", ex);
            request.setAttribute("error", "Login is temporarily unavailable. Please try again.");
            doGet(request, response);
        }
    }

    private String value(String input) {
        return input == null ? "" : input;
    }
}
