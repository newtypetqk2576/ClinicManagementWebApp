package regression;

import Dal.AppointmentDAO;
import Dal.AppointmentDAO.CreationResult;
import jakarta.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Runs the real DAO against a JDBC test double; no real database is accessed.
 * Covers transaction outcomes, bound values and resource cleanup, not SQL Server locking semantics.
 */
public final class AppointmentRegressionTest {
    private static final int PATIENT = 12;
    private static final int DOCTOR = 34;
    private static final LocalDateTime SLOT = LocalDateTime.of(2030, 5, 6, 9, 30);

    public static void main(String[] args) throws Exception {
        run("available slot commits the submitted appointment", () -> {
            Fixture fixture = new Fixture();
            equal(CreationResult.CREATED, create(fixture, "Khám tổng quát"));
            equal(1, fixture.commits);
            equal(0, fixture.rollbacks);
            equal(1, fixture.saved.size());
            Map<Integer, Object> row = fixture.saved.get(0);
            equal(PATIENT, row.get(1));
            equal(Timestamp.valueOf(SLOT), row.get(2));
            equal("Khám tổng quát", row.get(3));
            equal(DOCTOR, row.get(4));
            equal(List.of(Timestamp.valueOf(SLOT), DOCTOR, PATIENT), fixture.checkedSlot);
            fixture.assertClosed();
        });
        run("blank optional reason remains SQL NULL", () -> {
            Fixture fixture = new Fixture();
            equal(CreationResult.CREATED, create(fixture, null));
            check(fixture.saved.get(0).containsKey(3), "Reason parameter was not bound");
            equal(null, fixture.saved.get(0).get(3));
            fixture.assertClosed();
        });
        run("busy doctor rejects without inserting", () -> {
            Fixture fixture = new Fixture();
            fixture.conflictingDoctor = DOCTOR;
            equal(CreationResult.DOCTOR_BUSY, create(fixture, "New visit"));
            fixture.assertRejected(false);
        });
        run("busy patient with another doctor rejects without inserting", () -> {
            Fixture fixture = new Fixture();
            fixture.conflictingDoctor = DOCTOR + 1;
            equal(CreationResult.PATIENT_BUSY, create(fixture, "New visit"));
            fixture.assertRejected(false);
        });
        run("unavailable doctor rolls back", () -> {
            Fixture fixture = new Fixture();
            fixture.doctorAvailable = false;
            equal(CreationResult.DOCTOR_UNAVAILABLE, create(fixture, "New visit"));
            fixture.assertRejected(true);
        });
        run("insert failure rolls back and closes resources", () -> {
            Fixture fixture = new Fixture();
            fixture.failInsert = true;
            expectSQLException(() -> create(fixture, "New visit"));
            fixture.assertRejected(true);
        });
        run("conflict lookup failure rolls back and closes resources", () -> {
            Fixture fixture = new Fixture();
            fixture.failLookup = true;
            expectSQLException(() -> create(fixture, "New visit"));
            fixture.assertRejected(false);
        });
        System.out.println("Appointment regression checks passed: 7");
    }

    private static CreationResult create(Fixture fixture, String reason) throws Exception {
        Driver driver = new FixtureDriver(fixture);
        DriverManager.registerDriver(driver);
        try {
            ServletContext context = proxy(ServletContext.class, (object, method, args) -> {
                if (method.getName().equals("getResourceAsStream")) {
                    equal("/WEB-INF/ConnectDB.properties", args[0]);
                    String properties = "url=jdbc:appointment-regression:test\nuserID=test\npassword=test\n";
                    return new ByteArrayInputStream(properties.getBytes(StandardCharsets.UTF_8));
                }
                throw new UnsupportedOperationException(method.getName());
            });
            return new AppointmentDAO(context).createForPatient(PATIENT, DOCTOR, SLOT, reason);
        } finally {
            DriverManager.deregisterDriver(driver);
        }
    }

    private static final class Fixture {
        Integer conflictingDoctor;
        boolean doctorAvailable = true;
        boolean failInsert;
        boolean failLookup;
        boolean autoCommit = true;
        boolean closed;
        int commits;
        int rollbacks;
        int insertAttempts;
        int openStatements;
        int openResultSets;
        List<Object> checkedSlot;
        final List<Map<Integer, Object>> pending = new ArrayList<>();
        final List<Map<Integer, Object>> saved = new ArrayList<>();

        Connection connection() {
            return proxy(Connection.class, (object, method, args) -> {
                switch (method.getName()) {
                    case "getAutoCommit": return autoCommit;
                    case "setAutoCommit": autoCommit = (boolean) args[0]; return null;
                    case "prepareStatement": return statement((String) args[0]);
                    case "commit":
                        check(!autoCommit, "Commit must use an explicit transaction");
                        saved.addAll(pending);
                        pending.clear();
                        commits++;
                        return null;
                    case "rollback": pending.clear(); rollbacks++; return null;
                    case "close": closed = true; return null;
                    case "isClosed": return closed;
                    default: throw new UnsupportedOperationException(method.getName());
                }
            });
        }

        PreparedStatement statement(String sql) {
            boolean lookup = sql.startsWith("SELECT TOP (1)");
            boolean insert = sql.startsWith("INSERT INTO Appointments");
            check(lookup || insert, "Unexpected SQL in appointment creation: " + sql);
            openStatements++;
            Map<Integer, Object> parameters = new HashMap<>();
            return proxy(PreparedStatement.class, (object, method, args) -> {
                String name = method.getName();
                if (name.equals("setInt") || name.equals("setTimestamp") || name.equals("setNString")) {
                    parameters.put((int) args[0], args[1]);
                    return null;
                }
                if (name.equals("setNull")) {
                    parameters.put((int) args[0], null);
                    return null;
                }
                if (name.equals("close")) { openStatements--; return null; }
                check(!autoCommit, "Lookup and insert must share an explicit transaction");
                if (name.equals("executeQuery") && lookup) {
                    checkedSlot = List.of(parameters.get(1), parameters.get(2), parameters.get(3));
                    if (failLookup) { throw new SQLException("Injected lookup failure"); }
                    return conflictRows();
                }
                if (name.equals("executeUpdate") && insert) {
                    insertAttempts++;
                    if (failInsert) { throw new SQLException("Injected insert failure"); }
                    if (!doctorAvailable) { return 0; }
                    pending.add(new HashMap<>(parameters));
                    return 1;
                }
                throw new UnsupportedOperationException(name);
            });
        }

        ResultSet conflictRows() {
            openResultSets++;
            boolean[] read = {false};
            return proxy(ResultSet.class, (object, method, args) -> {
                switch (method.getName()) {
                    case "next":
                        boolean result = !read[0] && conflictingDoctor != null;
                        read[0] = true;
                        return result;
                    case "getInt": equal("DoctorID", args[0]); return conflictingDoctor;
                    case "close": openResultSets--; return null;
                    default: throw new UnsupportedOperationException(method.getName());
                }
            });
        }

        void assertRejected(boolean insertAttempted) {
            equal(0, commits);
            equal(1, rollbacks);
            equal(0, saved.size());
            equal(0, pending.size());
            equal(insertAttempted ? 1 : 0, insertAttempts);
            assertClosed();
        }

        void assertClosed() {
            check(closed, "Connection must close");
            check(autoCommit, "Original autocommit must be restored");
            equal(0, openStatements);
            equal(0, openResultSets);
        }
    }

    private static final class FixtureDriver implements Driver {
        private final Fixture fixture;
        FixtureDriver(Fixture fixture) { this.fixture = fixture; }
        @Override public Connection connect(String url, Properties info) {
            return acceptsURL(url) ? fixture.connection() : null;
        }
        @Override public boolean acceptsURL(String url) {
            return url != null && url.startsWith("jdbc:appointment-regression:");
        }
        @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }
        @Override public int getMajorVersion() { return 1; }
        @Override public int getMinorVersion() { return 0; }
        @Override public boolean jdbcCompliant() { return false; }
        @Override public Logger getParentLogger() { return Logger.getGlobal(); }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
    }

    private static void equal(Object expected, Object actual) {
        check(java.util.Objects.equals(expected, actual), "Expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }

    private static void expectSQLException(CheckedAction action) throws Exception {
        try {
            action.run();
            throw new AssertionError("Expected SQLException");
        } catch (SQLException expected) {
            // Expected failure; transaction/resource assertions are made by each test.
        }
    }

    private static void run(String name, CheckedAction action) throws Exception {
        action.run();
        System.out.println("PASS " + name);
    }

    @FunctionalInterface
    private interface CheckedAction { void run() throws Exception; }
}
