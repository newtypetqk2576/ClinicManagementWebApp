<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Create appointment</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Create appointment</h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:url var="appointmentsUrl" value="/doctor/appointments"/>
            <c:url var="patientSearchUrl" value="/doctor/patients/search"/>
            <form method="post" action="<c:out value="${appointmentsUrl}"/>">
                <input type="hidden" name="action" value="create">
                <label for="patientSearch">Search patient by name or phone</label>
                <input id="patientSearch" type="search" autocomplete="off"
                       data-search-url="<c:out value="${patientSearchUrl}"/>"
                       aria-describedby="patientSearchStatus">
                <p id="patientSearchStatus" role="status">Search to see matching patients.</p>
                <label for="patientID">Patient</label>
                <select id="patientID" name="patientID" required>
                    <option value="">Search above, then select a patient</option>
                </select>
                <label for="appointmentDate">Date and time</label>
                <input id="appointmentDate" type="datetime-local" name="appointmentDate"
                       value="<c:out value="${appointmentDateValue}"/>" required>
                <label for="reason">Reason</label>
                <textarea id="reason" name="reason" maxlength="255"><c:out value="${reasonValue}"/></textarea>
                <button type="submit">Create appointment</button>
            </form>
            <p><a href="<c:out value="${appointmentsUrl}"/>">Back to appointments</a></p>
        </main>
        <script>
            (() => {
                const searchInput = document.getElementById("patientSearch");
                const patientSelect = document.getElementById("patientID");
                const status = document.getElementById("patientSearchStatus");
                let debounceTimer;
                let activeRequest;

                searchInput.addEventListener("input", () => {
                    window.clearTimeout(debounceTimer);
                    if (activeRequest) {
                        activeRequest.abort();
                    }
                    patientSelect.replaceChildren(new Option("Search above, then select a patient", ""));
                    patientSelect.size = 1;
                    const term = searchInput.value.trim();
                    if (!term) {
                        status.textContent = "Search to see matching patients.";
                        return;
                    }

                    status.textContent = "Searching patients...";
                    debounceTimer = window.setTimeout(async () => {
                        activeRequest = new AbortController();
                        const url = new URL(searchInput.dataset.searchUrl, window.location.href);
                        url.searchParams.set("term", term);
                        try {
                            const result = await fetch(url, {
                                headers: {Accept: "application/json"},
                                signal: activeRequest.signal
                            });
                            if (!result.ok) {
                                throw new Error("Patient search failed.");
                            }
                            const patients = await result.json();
                            if (!Array.isArray(patients)
                                    || patients.some((patient) => !Number.isInteger(patient.id)
                                        || patient.id <= 0
                                        || typeof patient.name !== "string"
                                        || typeof patient.phone !== "string")) {
                                throw new Error("Patient search returned invalid results.");
                            }
                            if (patients.length === 0) {
                                status.textContent = "No matching patients were found.";
                                return;
                            }
                            patientSelect.size = Math.min(patients.length + 1, 6);
                            patients.forEach((patient) => {
                                const option = document.createElement("option");
                                option.value = patient.id;
                                option.textContent = patient.name + " — " + patient.phone;
                                patientSelect.append(option);
                            });
                            status.textContent = patients.length + " matching patient(s) found.";
                        } catch (error) {
                            if (error.name !== "AbortError") {
                                status.textContent = "Patient search is unavailable. Please try again.";
                            }
                        }
                    }, 250);
                });

                patientSelect.addEventListener("change", () => {
                    if (patientSelect.value) {
                        patientSelect.size = 1;
                    }
                });
            })();
        </script>
    </body>
</html>
