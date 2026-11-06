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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/doctor/services", "/doctor/services/new", "/doctor/services/edit"})
public class DoctorServicesServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorServicesServlet.class.getName());
    private static final String LIST_PAGE = "/view/doctor-services.jsp";
    private static final String FORM_PAGE = "/view/doctor-service-form.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        if (!requireManagerOrDoctor(request, response)) {
            return;
        }

        String path = request.getServletPath();
        if ("/doctor/services/new".equals(path)) {
            Service service = new Service();
            service.setActive(true);
            renderForm(request, response, service, true, "");
        } else if ("/doctor/services/edit".equals(path)) {
            try {
                int id = parseID(request.getParameter("id"));
                Service service = new ServiceDAO(getServletContext()).getByID(id);
                if (service == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                renderForm(request, response, service, false, service.getPrice().toPlainString());
            } catch (NumberFormatException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Unable to load doctor service.", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Service information is temporarily unavailable.");
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
        if (!requireManagerOrDoctor(request, response)) {
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
        Service service;
        if (creating) {
            service = new Service();
        } else {
            try {
                int id = parseID(request.getParameter("id"));
                service = new ServiceDAO(getServletContext()).getByID(id);
                if (service == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } catch (NumberFormatException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Unable to load service for update.", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Service information is temporarily unavailable.");
                return;
            }
        }

        service.setServiceName(value(request.getParameter("serviceName")).trim());
        service.setDescription(blankToNull(request.getParameter("description")));
        String priceValue = value(request.getParameter("price")).trim();
        service.setPrice(parsePrice(priceValue));
        String activeValue = request.getParameter("isActive");
        service.setActive("true".equals(activeValue));
        String error = validate(service, priceValue, activeValue);
        if (error != null) {
            renderForm(request, response, service, creating, priceValue, error);
            return;
        }

        try {
            ServiceDAO dao = new ServiceDAO(getServletContext());
            if (creating) {
                dao.create(service);
            } else if (!dao.update(service)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.getSession(false).setAttribute("doctorServiceMessage",
                    creating ? "Service created." : "Service changes saved.");
            response.sendRedirect(request.getContextPath() + "/doctor/services");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to save doctor service.", ex);
            renderForm(request, response, service, creating, priceValue,
                    "Service could not be saved right now. Please try again.");
        }
    }

    private String validate(Service service, String priceValue, String activeValue) {
        if (service.getServiceName().isEmpty() || service.getServiceName().length() > 150) {
            return "Service name is required and must be at most 150 characters.";
        }
        if (service.getDescription() != null && service.getDescription().length() > 500) {
            return "Description must be at most 500 characters.";
        }
        if (!priceValue.matches("[0-9]{1,10}(\\.[0-9]{1,2})?")
                || service.getPrice() == null) {
            return "Price must be a non-negative number with up to 10 digits and 2 decimal places.";
        }
        if (activeValue != null && !"true".equals(activeValue)) {
            return "Invalid service status.";
        }
        return null;
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        request.setAttribute("message", session.getAttribute("doctorServiceMessage"));
        session.removeAttribute("doctorServiceMessage");
        try {
            List<Service> services = new ServiceDAO(getServletContext()).getAll();
            ListPage.byName(request, "services", services,
                    "Tên dịch vụ", Service::getServiceName);
            request.getRequestDispatcher(LIST_PAGE).forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load doctor service list.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Service list is temporarily unavailable.");
        }
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response, Service service,
            boolean creating, String priceValue) throws ServletException, IOException {
        renderForm(request, response, service, creating, priceValue, null);
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response, Service service,
            boolean creating, String priceValue, String error) throws ServletException, IOException {
        request.setAttribute("service", service);
        request.setAttribute("createMode", creating);
        request.setAttribute("priceValue", priceValue);
        request.setAttribute("error", error);
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
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

    private BigDecimal parsePrice(String value) {
        if (!value.matches("[0-9]{1,10}(\\.[0-9]{1,2})?")) {
            return null;
        }
        return new BigDecimal(value);
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
