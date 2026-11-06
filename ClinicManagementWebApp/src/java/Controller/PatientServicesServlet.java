package Controller;

import Dal.ServiceDAO;
import Model.Service;
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

@WebServlet("/services")
public class PatientServicesServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientServicesServlet.class.getName());
    private static final String PAGE = "/view/patient-services.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        if (!requirePatient(request, response)) {
            return;
        }
        try {
            ServiceDAO serviceDAO = new ServiceDAO(getServletContext());
            List<Service> services = serviceDAO.getActiveServices();
            ListPage.byName(request, "services", services,
                    "Tên dịch vụ", Service::getServiceName);
            request.getRequestDispatcher(PAGE).forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load active patient services.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Services are temporarily unavailable.");
        }
    }

    private boolean requirePatient(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("UserID") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!"PATIENT".equals(session.getAttribute("Role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        Object patientID = session.getAttribute("PatientID");
        if (!(patientID instanceof Integer id) || id <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
