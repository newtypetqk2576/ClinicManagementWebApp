package regression;

import Controller.DoctorMedicalRecordsServlet;
import Dal.MedicalRecordDAO;
import Dal.MedicalRecordDAO.InvoicePaymentResult;
import Model.MedicalRecord;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standalone regression checks; no database, network, or extra test dependency.
 * Uses the real servlet and DAO with an in-memory JDBC test double. This checks
 * submitted values, rollback and retry behavior, not SQL Server lock semantics.
 * Compile with application classes, servlet-api.jar and the bundled JDBC jar.
 * Run: java ... regression.PaymentRegressionTest
 */
public final class PaymentRegressionTest {
    private static final String URL = "jdbc:clinic-payment-regression:test";
    private static Database database;
    private static int passed;

    public static void main(String[] args) throws Exception {
        Driver driver = new Driver() {
            public boolean acceptsURL(String url) { return URL.equals(url); }
            public Connection connect(String url, Properties info) {
                return acceptsURL(url) ? database.connection() : null;
            }
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
                return new DriverPropertyInfo[0];
            }
            public int getMajorVersion() { return 1; }
            public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public Logger getParentLogger() { return Logger.getGlobal(); }
        };
        DriverManager.registerDriver(driver);
        Logger servletLogger = Logger.getLogger(DoctorMedicalRecordsServlet.class.getName());
        Level oldLevel = servletLogger.getLevel();
        servletLogger.setLevel(Level.OFF); // Expected injected-failure case.
        try {
            check("manager saves unsaved Unicode text with payment", () -> {
                reset();
                equal(InvoicePaymentResult.CONFIRMED, dao().confirmInvoicePaymentForManager(edited()));
                assertPaidText();
                equal(1, database.commits);
            });
            check("doctor saves all fields with payment", () -> {
                reset();
                equal(InvoicePaymentResult.CONFIRMED, dao().confirmInvoicePaymentForDoctor(edited(), 9));
                assertPaidText();
            });
            check("blank and null clinical fields intentionally clear old text", () -> {
                reset();
                MedicalRecord edit = edited();
                edit.setSymptoms("   ");
                edit.setDiagnosis(null);
                edit.setTreatmentNotes("");
                dao().confirmInvoicePaymentForManager(edit);
                equal(null, database.data.symptoms);
                equal(null, database.data.diagnosis);
                equal(null, database.data.notes);
            });
            check("failure after invoice update rolls everything back", () -> {
                reset();
                database.failRecordWrite = true;
                expectSqlFailure(() -> dao().confirmInvoicePaymentForManager(edited()));
                assertUnchanged();
                equal(1, database.rollbacks);
                equal(0, database.commits);
            });
            check("zero-row record update rolls payment back", () -> {
                reset();
                database.skipRecordWrite = true;
                expectSqlFailure(() -> dao().confirmInvoicePaymentForManager(edited()));
                assertUnchanged();
            });
            check("already-paid retry never overwrites text or paid time", () -> {
                reset();
                dao().confirmInvoicePaymentForManager(edited());
                int writes = database.recordWrites;
                MedicalRecord stale = edited();
                stale.setDiagnosis("stale browser tab");
                equal(InvoicePaymentResult.ALREADY_PAID, dao().confirmInvoicePaymentForManager(stale));
                assertPaidText();
                equal(writes, database.recordWrites);
                equal(1, database.data.paidAt);
            });
            check("another doctor's record remains untouched", () -> {
                reset();
                equal(InvoicePaymentResult.NOT_FOUND, dao().confirmInvoicePaymentForDoctor(edited(), 99));
                assertUnchanged();
            });
            check("missing invoice remains unpaid and editable", () -> {
                reset();
                database.data.invoiceExists = false;
                equal(InvoicePaymentResult.NOT_FOUND, dao().confirmInvoicePaymentForManager(edited()));
                assertUnchanged();
            });
            for (String status : List.of("CANCELLED", "COMPLETED")) {
                check(status + " record cannot be paid", () -> {
                    reset();
                    database.data.recordStatus = status;
                    equal(InvoicePaymentResult.NOT_PAYABLE, dao().confirmInvoicePaymentForManager(edited()));
                    equal(status, database.data.recordStatus);
                    equal("UNPAID", database.data.paymentStatus);
                    equal("old diagnosis", database.data.diagnosis);
                });
            }
            check("cancelled invoice cannot be paid", () -> {
                reset();
                database.data.paymentStatus = "CANCELLED";
                equal(InvoicePaymentResult.NOT_PAYABLE, dao().confirmInvoicePaymentForManager(edited()));
                equal("CANCELLED", database.data.paymentStatus);
                equal("old diagnosis", database.data.diagnosis);
            });
            check("legacy schema without PaidAt still saves clinical fields", () -> {
                reset();
                database.hasPaidAt = false;
                equal(InvoicePaymentResult.CONFIRMED, dao().confirmInvoicePaymentForManager(edited()));
                assertPaidText();
                equal(0, database.data.paidAt);
            });
            check("real servlet passes unsaved clinical input to payment", () -> {
                reset();
                WebCall call = submit(parameters());
                equal(0, call.errorStatus);
                equal("/clinic/admin/medical-records?id=17", call.redirect);
                assertPaidText();
            });
            check("validation failure preserves input and does not collect payment", () -> {
                reset();
                Map<String, String> params = parameters();
                params.put("diagnosis", "x".repeat(1001));
                WebCall call = submit(params);
                assertReturnedText(call, params.get("diagnosis"));
                assertUnchanged();
            });
            check("cancel choice blocks payment while retaining clinical text", () -> {
                reset();
                Map<String, String> params = parameters();
                params.put("status", "CANCELLED");
                assertReturnedText(submit(params), edited().getDiagnosis());
                assertUnchanged();
            });
            check("missing clinical field rejects partial payment request", () -> {
                reset();
                Map<String, String> params = parameters();
                params.remove("diagnosis");
                equal(400, submit(params).errorStatus);
                assertUnchanged();
            });
            check("database failure preserves form text and rolls back payment", () -> {
                reset();
                database.failRecordWrite = true;
                assertReturnedText(submit(parameters()), edited().getDiagnosis());
                assertUnchanged();
            });
            check("servlet retry leaves completed contents unchanged", () -> {
                reset();
                submit(parameters());
                Map<String, String> params = parameters();
                params.put("diagnosis", "stale browser tab");
                equal("/clinic/admin/medical-records?id=17", submit(params).redirect);
                assertPaidText();
                equal(1, database.recordWrites);
            });
            System.out.println("PASS: " + passed + " payment regression scenarios (JDBC test double).");
        } finally {
            servletLogger.setLevel(oldLevel);
            DriverManager.deregisterDriver(driver);
        }
    }

    private static void reset() { database = new Database(); }
    private static MedicalRecordDAO dao() { return new MedicalRecordDAO(context()); }
    private static MedicalRecord edited() {
        MedicalRecord record = new MedicalRecord();
        record.setMedicalRecordID(17);
        record.setDoctorID(9);
        record.setStatus("IN_PROGRESS");
        record.setSymptoms("Triệu chứng mới: đau đầu");
        record.setDiagnosis("Chẩn đoán đã cập nhật");
        record.setTreatmentNotes("Ghi chú điều trị mới\nDòng thứ hai");
        return record;
    }
    private static Map<String, String> parameters() {
        MedicalRecord record = edited();
        return new HashMap<>(Map.of("id", "17", "action", "update", "status", "IN_PROGRESS",
                "symptoms", record.getSymptoms(), "diagnosis", record.getDiagnosis(),
                "treatmentNotes", record.getTreatmentNotes()));
    }
    private static void assertPaidText() {
        equal("PAID", database.data.paymentStatus);
        equal("COMPLETED", database.data.recordStatus);
        equal(edited().getSymptoms(), database.data.symptoms);
        equal(edited().getDiagnosis(), database.data.diagnosis);
        equal(edited().getTreatmentNotes(), database.data.notes);
    }
    private static void assertUnchanged() {
        equal("UNPAID", database.data.paymentStatus);
        equal("IN_PROGRESS", database.data.recordStatus);
        equal("old symptoms", database.data.symptoms);
        equal("old diagnosis", database.data.diagnosis);
        equal("old notes", database.data.notes);
        equal(0, database.data.paidAt);
    }
    private static void assertReturnedText(WebCall call, String diagnosis) {
        equal("/view/doctor-medical-record-form.jsp", call.forward);
        equal(null, call.redirect);
        equal(diagnosis, ((MedicalRecord) call.attributes.get("record")).getDiagnosis());
        if (call.attributes.get("error") == null) throw new AssertionError("Expected form error");
    }
    private static void equal(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected <" + expected + "> but got <" + actual + ">");
        }
    }
    private interface Checked { void run() throws Exception; }
    private static void check(String name, Checked test) throws Exception {
        try { test.run(); passed++; }
        catch (Throwable ex) { throw new AssertionError(name, ex); }
    }
    private static void expectSqlFailure(Checked test) throws Exception {
        try { test.run(); }
        catch (SQLException expected) { return; }
        throw new AssertionError("Expected injected SQLException");
    }
    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }
    private static ServletContext context() {
        return proxy(ServletContext.class, (p, method, args) -> {
            if (method.getName().equals("getResourceAsStream")) {
                return new ByteArrayInputStream(("url=" + URL + "\nuserID=test\npassword=test\n")
                        .getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });
    }
    private static final class WebCall {
        final Map<String, Object> attributes = new HashMap<>();
        String forward;
        String redirect;
        int errorStatus;
    }
    private static WebCall submit(Map<String, String> params) throws Exception {
        WebCall result = new WebCall();
        Map<String, Object> sessionValues = new HashMap<>(Map.of("UserID", 1, "Role", "MANAGER"));
        HttpSession session = proxy(HttpSession.class, (p, method, args) -> {
            if (method.getName().equals("getAttribute")) return sessionValues.get(args[0]);
            if (method.getName().equals("setAttribute")) sessionValues.put((String) args[0], args[1]);
            return null;
        });
        HttpServletRequest request = proxy(HttpServletRequest.class, (p, method, args) -> {
            return switch (method.getName()) {
                case "getMethod" -> "POST";
                case "getServletPath" -> "/admin/medical-records/confirm-payment";
                case "getContextPath" -> "/clinic";
                case "getParameter" -> params.get(args[0]);
                case "getSession" -> session;
                case "setAttribute" -> { result.attributes.put((String) args[0], args[1]); yield null; }
                case "getAttribute" -> result.attributes.get(args[0]);
                case "getRequestDispatcher" -> proxy(RequestDispatcher.class, (d, m, a) -> {
                    if (m.getName().equals("forward")) result.forward = (String) args[0];
                    return null;
                });
                default -> null;
            };
        });
        HttpServletResponse response = proxy(HttpServletResponse.class, (p, method, args) -> {
            if (method.getName().equals("sendRedirect")) result.redirect = (String) args[0];
            if (method.getName().equals("sendError")) result.errorStatus = (Integer) args[0];
            return null;
        });
        DoctorMedicalRecordsServlet servlet = new DoctorMedicalRecordsServlet();
        servlet.init(proxy(ServletConfig.class, (p, method, args) ->
                method.getName().equals("getServletContext") ? context() : null));
        servlet.service(request, response);
        return result;
    }

    private static final class Data implements Cloneable {
        boolean invoiceExists = true;
        String recordStatus = "IN_PROGRESS";
        String paymentStatus = "UNPAID";
        String symptoms = "old symptoms";
        String diagnosis = "old diagnosis";
        String notes = "old notes";
        int paidAt;
        Data copy() {
            try { return (Data) clone(); }
            catch (CloneNotSupportedException ex) { throw new AssertionError(ex); }
        }
    }
    private static final class Database {
        Data data = new Data();
        boolean hasPaidAt = true;
        boolean failRecordWrite;
        boolean skipRecordWrite;
        int commits;
        int rollbacks;
        int recordWrites;

        Connection connection() {
            return proxy(Connection.class, new InvocationHandler() {
                boolean autoCommit = true;
                Data transaction;
                public Object invoke(Object p, java.lang.reflect.Method method, Object[] args) throws Throwable {
                    return switch (method.getName()) {
                        case "getAutoCommit" -> autoCommit;
                        case "setAutoCommit" -> {
                            boolean next = (Boolean) args[0];
                            if (autoCommit && !next) transaction = data.copy();
                            autoCommit = next;
                            yield null;
                        }
                        case "prepareStatement" -> statement((String) args[0], () -> autoCommit ? data : transaction);
                        case "commit" -> { data = transaction.copy(); commits++; yield null; }
                        case "rollback" -> { transaction = data.copy(); rollbacks++; yield null; }
                        case "close" -> null;
                        default -> throw new UnsupportedOperationException("Connection." + method.getName());
                    };
                }
            });
        }
        private interface CurrentData { Data get(); }
        PreparedStatement statement(String sql, CurrentData current) {
            Map<Integer, Object> parameters = new HashMap<>();
            return proxy(PreparedStatement.class, (p, method, args) -> {
                if (method.getName().startsWith("set")) {
                    parameters.put((Integer) args[0], method.getName().equals("setNull") ? null : args[1]);
                    return null;
                }
                return switch (method.getName()) {
                    case "executeQuery" -> rows(query(sql, parameters, current.get()));
                    case "executeUpdate" -> update(sql, parameters, current.get());
                    case "close" -> null;
                    default -> throw new UnsupportedOperationException("Statement." + method.getName());
                };
            });
        }
        boolean inScope(String sql, Map<Integer, Object> params, int idIndex) {
            return Objects.equals(17, params.get(idIndex))
                    && (!sql.contains("AND mr.DoctorID = ?") || Objects.equals(9, params.get(idIndex + 1)));
        }
        List<Map<String, Object>> query(String sql, Map<Integer, Object> params, Data current) {
            if (sql.startsWith("SELECT COL_LENGTH")) {
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("columnLength", hasPaidAt ? 8 : null);
                return List.of(value);
            }
            if (sql.startsWith("SELECT mr.MedicalRecordID FROM MedicalRecords")) {
                return inScope(sql, params, 1) ? List.of(Map.of("MedicalRecordID", 17)) : List.of();
            }
            if (sql.startsWith("SELECT i.PaymentStatus")) {
                return current.invoiceExists && inScope(sql, params, 1)
                        ? List.of(Map.of("PaymentStatus", current.paymentStatus, "Status", current.recordStatus)) : List.of();
            }
            if (sql.contains("FROM MedicalRecordServices") || sql.contains("FROM Prescriptions p")) return List.of();
            if (sql.startsWith("SELECT InvoiceID")) {
                return current.invoiceExists ? List.of(Map.of("InvoiceID", 31,
                        "InvoiceDate", Timestamp.valueOf("2026-11-06 09:00:00"),
                        "ServiceAmount", BigDecimal.TEN, "MedicineAmount", BigDecimal.ZERO,
                        "DiscountAmount", BigDecimal.ZERO, "TotalAmount", BigDecimal.TEN,
                        "PaymentStatus", current.paymentStatus)) : List.of();
            }
            if (sql.startsWith("SELECT mr.MedicalRecordID,")) {
                if (!inScope(sql, params, 1)) return List.of();
                Map<String, Object> row = new HashMap<>();
                row.put("MedicalRecordID", 17); row.put("PatientID", 3); row.put("DoctorID", 9);
                row.put("PatientName", "Patient"); row.put("DoctorName", "Doctor");
                row.put("VisitDate", Timestamp.valueOf("2026-11-06 09:00:00"));
                row.put("CreatedAt", Timestamp.valueOf("2026-11-06 09:00:00"));
                row.put("Status", current.recordStatus); row.put("Symptoms", current.symptoms);
                row.put("Diagnosis", current.diagnosis); row.put("TreatmentNotes", current.notes);
                return List.of(row);
            }
            throw new AssertionError("Unexpected query: " + sql);
        }
        int update(String sql, Map<Integer, Object> params, Data current) throws SQLException {
            boolean editable = !List.of("COMPLETED", "CANCELLED").contains(current.recordStatus);
            if (sql.startsWith("UPDATE i SET PaymentStatus = 'PAID'")) {
                if (!current.invoiceExists || !editable || !"UNPAID".equals(current.paymentStatus)
                        || !inScope(sql, params, 1)) return 0;
                current.paymentStatus = "PAID";
                if (sql.contains("PaidAt = SYSDATETIME()")) current.paidAt++;
                return 1;
            }
            if (sql.startsWith("UPDATE mr SET Symptoms = ?")) {
                if (failRecordWrite) throw new SQLException("Injected record-write failure");
                if (skipRecordWrite || !current.invoiceExists || !editable
                        || !"PAID".equals(current.paymentStatus) || !inScope(sql, params, 4)) return 0;
                current.symptoms = (String) params.get(1);
                current.diagnosis = (String) params.get(2);
                current.notes = (String) params.get(3);
                current.recordStatus = "COMPLETED";
                recordWrites++;
                return 1;
            }
            throw new AssertionError("Unexpected update: " + sql);
        }
    }
    private static ResultSet rows(List<Map<String, Object>> values) {
        return proxy(ResultSet.class, new InvocationHandler() {
            int index = -1;
            boolean wasNull;
            public Object invoke(Object p, java.lang.reflect.Method method, Object[] args) {
                if (method.getName().equals("next")) return ++index < values.size();
                if (method.getName().equals("close")) return null;
                if (method.getName().equals("wasNull")) return wasNull;
                Object key = args[0];
                Object value = key instanceof Integer number
                        ? values.get(index).values().toArray()[number - 1] : values.get(index).get(key);
                wasNull = value == null;
                return switch (method.getName()) {
                    case "getInt" -> value == null ? 0 : ((Number) value).intValue();
                    case "getString", "getObject", "getTimestamp", "getBigDecimal" -> value;
                    default -> throw new UnsupportedOperationException("ResultSet." + method.getName());
                };
            }
        });
    }
}
