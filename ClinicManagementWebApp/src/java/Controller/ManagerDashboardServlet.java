package Controller;

import Dal.MedicalRecordDAO;
import Model.MedicalRecord;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin/dashboard")
public class ManagerDashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ManagerDashboardServlet.class.getName());
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("dd/MM");
    private static final List<String> WEEKDAYS = List.of("Thứ 2", "Thứ 3", "Thứ 4",
            "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("UserID") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!"MANAGER".equals(session.getAttribute("Role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        LocalDate today = LocalDate.now(CLINIC_ZONE);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        try {
            Map<LocalDate, BigDecimal> revenue = new MedicalRecordDAO(getServletContext())
                    .getDailyRevenue(monday, today.plusDays(1));
            request.setAttribute("todayLabel", DATE.format(today));
            request.setAttribute("weekStart", DATE.format(monday));
            request.setAttribute("weekEnd", DATE.format(monday.plusDays(6)));
            request.setAttribute("todayRevenue", revenue.getOrDefault(today, BigDecimal.ZERO));
            request.setAttribute("weekRevenue", revenue.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
            request.setAttribute("revenueChart", chart(monday, today, revenue));
            List<MedicalRecord> records = new MedicalRecordDAO(getServletContext()).getForManagerOn(today);
            ListPage.byName(request, "records", records, "Tên bệnh nhân hoặc bác sĩ",
                    row -> row.getPatientName() + " " + row.getDoctorName());
            request.setAttribute("routeBase", "/admin/medical-records");
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load manager dashboard.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Hiện chưa thể tải trang tổng quan.");
        }
    }

    private List<Map<String, Object>> chart(LocalDate monday, LocalDate today,
            Map<LocalDate, BigDecimal> revenue) {
        BigDecimal peak = revenue.isEmpty() ? BigDecimal.ZERO : Collections.max(revenue.values());
        List<Map<String, Object>> days = new ArrayList<>(7);
        for (int index = 0; index < WEEKDAYS.size(); index++) {
            LocalDate date = monday.plusDays(index);
            BigDecimal total = revenue.getOrDefault(date, BigDecimal.ZERO);
            BigDecimal percent = peak.signum() == 0 ? BigDecimal.ZERO
                    : total.scaleByPowerOfTen(2).divide(peak, 2, RoundingMode.HALF_UP);
            days.add(Map.of("weekday", WEEKDAYS.get(index), "date", SHORT_DATE.format(date),
                    "total", total, "height", percent.toPlainString(), "today", date.equals(today)));
        }
        return days;
    }
}
