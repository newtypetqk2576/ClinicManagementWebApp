package Controller;

import Dal.UserDAO;
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

@WebServlet("/account/change-password")
public class PasswordChangeServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PasswordChangeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/auth/change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String currentPassword = value(request.getParameter("currentPassword"));
        String newPassword = value(request.getParameter("newPassword"));
        String confirmPassword = value(request.getParameter("confirmPassword"));
        if (currentPassword.isEmpty() || newPassword.isEmpty() || newPassword.length() > 255) {
            forwardError(request, response, "Enter the current password and a new password of at most 255 characters.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            forwardError(request, response, "The new passwords do not match.");
            return;
        }

        HttpSession session = request.getSession(false);
        Object userIDValue = session == null ? null : session.getAttribute("UserID");
        if (!(userIDValue instanceof Integer userID)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            if (!new UserDAO(getServletContext()).changePassword(
                    userID, currentPassword, newPassword)) {
                forwardError(request, response, "The current password is incorrect.");
                return;
            }
            request.setAttribute("success", "Password changed.");
            doGet(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to change password.", ex);
            forwardError(request, response,
                    "Password change is temporarily unavailable. Please try again.");
        }
    }

    private void forwardError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        doGet(request, response);
    }

    private String value(String input) {
        return input == null ? "" : input;
    }
}
