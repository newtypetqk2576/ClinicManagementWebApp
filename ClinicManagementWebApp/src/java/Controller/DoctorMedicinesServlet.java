package Controller;

import Dal.MedicineDAO;
import Model.Medicine;
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

@WebServlet(urlPatterns = {"/doctor/medicines", "/doctor/medicines/new", "/doctor/medicines/edit"})
public class DoctorMedicinesServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorMedicinesServlet.class.getName());
    private static final String LIST_PAGE = "/view/doctor-medicines.jsp";
    private static final String FORM_PAGE = "/view/doctor-medicine-form.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        if (!requireManagerOrDoctor(request, response)) {
            return;
        }

        String path = request.getServletPath();
        if ("/doctor/medicines/new".equals(path)) {
            Medicine medicine = new Medicine();
            medicine.setActive(true);
            renderForm(request, response, medicine, true, "", "0");
        } else if ("/doctor/medicines/edit".equals(path)) {
            try {
                int id = parseID(request.getParameter("id"));
                Medicine medicine = new MedicineDAO(getServletContext()).getByID(id);
                if (medicine == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                renderForm(request, response, medicine, false, medicine.getPrice().toPlainString(),
                        Integer.toString(medicine.getStockQuantity()));
            } catch (NumberFormatException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Unable to load doctor medicine.", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Medicine information is temporarily unavailable.");
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
        Medicine medicine;
        if (creating) {
            medicine = new Medicine();
        } else {
            try {
                int id = parseID(request.getParameter("id"));
                medicine = new MedicineDAO(getServletContext()).getByID(id);
                if (medicine == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } catch (NumberFormatException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Unable to load medicine for update.", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Medicine information is temporarily unavailable.");
                return;
            }
        }

        medicine.setMedicineName(value(request.getParameter("medicineName")).trim());
        medicine.setActiveIngredient(blankToNull(request.getParameter("activeIngredient")));
        medicine.setUnit(value(request.getParameter("unit")).trim());
        String priceValue = value(request.getParameter("price")).trim();
        medicine.setPrice(parsePrice(priceValue));
        String stockValue = value(request.getParameter("stockQuantity")).trim();
        medicine.setStockQuantity(parseStock(stockValue));
        String activeValue = request.getParameter("isActive");
        medicine.setActive("true".equals(activeValue));
        String error = validate(medicine, priceValue, stockValue, activeValue);
        if (error != null) {
            renderForm(request, response, medicine, creating, priceValue, stockValue, error);
            return;
        }

        try {
            MedicineDAO dao = new MedicineDAO(getServletContext());
            if (creating) {
                dao.create(medicine);
            } else if (!dao.update(medicine)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.getSession(false).setAttribute("doctorMedicineMessage",
                    creating ? "Medicine created." : "Medicine changes saved.");
            response.sendRedirect(request.getContextPath() + "/doctor/medicines");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to save doctor medicine.", ex);
            renderForm(request, response, medicine, creating, priceValue, stockValue,
                    "Medicine could not be saved right now. Please try again.");
        }
    }

    private String validate(Medicine medicine, String priceValue, String stockValue, String activeValue) {
        if (medicine.getMedicineName().isEmpty() || medicine.getMedicineName().length() > 150) {
            return "Medicine name is required and must be at most 150 characters.";
        }
        if (medicine.getActiveIngredient() != null && medicine.getActiveIngredient().length() > 150) {
            return "Active ingredient must be at most 150 characters.";
        }
        if (medicine.getUnit().isEmpty() || medicine.getUnit().length() > 30) {
            return "Unit is required and must be at most 30 characters.";
        }
        if (!priceValue.matches("[0-9]{1,10}(\\.[0-9]{1,2})?")
                || medicine.getPrice() == null) {
            return "Price must be a non-negative number with up to 10 digits and 2 decimal places.";
        }
        if (!stockValue.matches("[0-9]+") || !isValidStock(stockValue)) {
            return "Stock quantity must be a non-negative whole number.";
        }
        if (activeValue != null && !"true".equals(activeValue)) {
            return "Invalid medicine status.";
        }
        return null;
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        request.setAttribute("message", session.getAttribute("doctorMedicineMessage"));
        session.removeAttribute("doctorMedicineMessage");
        try {
            List<Medicine> medicines = new MedicineDAO(getServletContext()).getAll();
            request.setAttribute("medicines", medicines);
            request.getRequestDispatcher(LIST_PAGE).forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load doctor medicine list.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Medicine list is temporarily unavailable.");
        }
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response, Medicine medicine,
            boolean creating, String priceValue, String stockValue) throws ServletException, IOException {
        renderForm(request, response, medicine, creating, priceValue, stockValue, null);
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response, Medicine medicine,
            boolean creating, String priceValue, String stockValue, String error)
            throws ServletException, IOException {
        request.setAttribute("medicine", medicine);
        request.setAttribute("createMode", creating);
        request.setAttribute("priceValue", priceValue);
        request.setAttribute("stockValue", stockValue);
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

    private int parseStock(String value) {
        if (!value.matches("[0-9]+")) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private boolean isValidStock(String value) {
        return parseStock(value) >= 0;
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
