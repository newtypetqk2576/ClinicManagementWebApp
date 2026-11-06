package Controller;

import Dal.ServiceDAO;
import Model.Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet({"/home", "/index.html"})
public class HomeServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(HomeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        request.setAttribute("listAnchor", "#dich-vu");
        try {
            ListPage.byName(request, "services", new ServiceDAO(getServletContext()).getActiveServices(),
                    "Tên dịch vụ", Service::getServiceName);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Unable to load the public service catalog.", ex);
            request.setAttribute("catalogUnavailable", true);
        }
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
