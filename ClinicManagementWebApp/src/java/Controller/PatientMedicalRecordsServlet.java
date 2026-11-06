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
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/medical-records")
public class PatientMedicalRecordsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientMedicalRecordsServlet.class.getName());
    private static final String LIST_PAGE = "/view/patient-medical-records.jsp";
    private static final String DETAIL_PAGE = "/view/patient-medical-record-detail.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, private");
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("UserID") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!"PATIENT".equals(session.getAttribute("Role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Object patientValue = session.getAttribute("PatientID");
        if (!(patientValue instanceof Integer patientID) || patientID <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String recordValue = request.getParameter("id");
        Integer medicalRecordID = null;
        if (recordValue != null) {
            try {
                medicalRecordID = Integer.valueOf(recordValue);
                if (medicalRecordID <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        try {
            MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO(getServletContext());
            if (medicalRecordID == null) {
                List<MedicalRecord> records = medicalRecordDAO.getByPatientID(patientID);
                ListPage.byNameAndDate(request, "records", records,
                        "Tên bác sĩ", MedicalRecord::getDoctorName,
                        "Ngày khám", MedicalRecord::getVisitDate);
                request.getRequestDispatcher(LIST_PAGE).forward(request, response);
                return;
            }

            MedicalRecord record = medicalRecordDAO.getDetailsForPatient(medicalRecordID, patientID);
            if (record == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("record", record);
            request.getRequestDispatcher(DETAIL_PAGE).forward(request, response);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to load patient medical records.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Medical records are temporarily unavailable.");
        }
    }
}
