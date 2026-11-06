<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Đặt lịch khám</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Đặt lịch khám</h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:url var="appointmentsUrl" value="${routeBase}"/>
            <c:choose>
                <c:when test="${managerMode}">
                    <c:url var="patientSearchUrl" value="/admin/appointments/search-patients"/>
                </c:when>
                <c:otherwise>
                    <c:url var="patientSearchUrl" value="/doctor/patients/search"/>
                </c:otherwise>
            </c:choose>
            <c:url var="doctorSearchUrl" value="${routeBase}/search-doctors"/>
            <form method="post" action="<c:out value="${appointmentsUrl}"/>">
                <input type="hidden" name="action" value="create">
                <label for="patientSearch">Tìm bệnh nhân theo tên hoặc số điện thoại</label>
                <input id="patientSearch" type="search" autocomplete="off" maxlength="100"
                       data-search-url="<c:out value="${patientSearchUrl}"/>"
                       aria-describedby="patientSearchStatus">
                <p id="patientSearchStatus" role="status">Nhập từ khóa để tìm bệnh nhân.</p>
                <label for="patientID">Bệnh nhân</label>
                <select id="patientID" name="patientID" required>
                    <option value="">Tìm và chọn bệnh nhân</option>
                </select>
                <c:if test="${managerMode}">
                    <label for="doctorSearch">Tìm bác sĩ theo tên hoặc số điện thoại</label>
                    <input id="doctorSearch" type="search" autocomplete="off" maxlength="100"
                           data-search-url="<c:out value="${doctorSearchUrl}"/>"
                           aria-describedby="doctorSearchStatus">
                    <p id="doctorSearchStatus" role="status">Nhập từ khóa để tìm bác sĩ đang hoạt động.</p>
                    <label for="doctorID">Bác sĩ</label>
                    <select id="doctorID" name="doctorID" required>
                        <option value="">Tìm và chọn bác sĩ</option>
                        <c:if test="${not empty doctorIDValue}">
                            <option value="<c:out value="${doctorIDValue}"/>" selected>Tìm lại để chọn bác sĩ.</option>
                        </c:if>
                    </select>
                </c:if>
                <label for="appointmentDate">Ngày giờ hẹn</label>
                <input id="appointmentDate" type="datetime-local" name="appointmentDate"
                       value="<c:out value="${appointmentDateValue}"/>" required>
                <label for="reason">Lý do khám</label>
                <textarea id="reason" name="reason" maxlength="255"><c:out value="${reasonValue}"/></textarea>
                <button type="submit">Đặt lịch khám</button>
            </form>
            <p><a href="<c:out value="${appointmentsUrl}"/>">Về danh sách lịch hẹn</a></p>
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
                        status.textContent = "Nhập từ khóa để tìm bệnh nhân.";
                        return;
                    }

                    status.textContent = "Đang tìm bệnh nhân…";
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
                                status.textContent = "Không tìm thấy bệnh nhân.";
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
                                status.textContent = "Chưa thể tìm bệnh nhân. Vui lòng thử lại.";
                            }
                        }
                    }, 250);
                });

                patientSelect.addEventListener("change", () => {
                    if (patientSelect.value) {
                        patientSelect.size = 1;
                    }
                });

                const doctorSearch = document.getElementById("doctorSearch");
                if (doctorSearch) {
                    const doctorSelect = document.getElementById("doctorID");
                    const doctorStatus = document.getElementById("doctorSearchStatus");
                    let doctorTimer;
                    let doctorRequest;
                    doctorSearch.addEventListener("input", () => {
                        window.clearTimeout(doctorTimer);
                        if (doctorRequest) {
                            doctorRequest.abort();
                        }
                        doctorSelect.replaceChildren(new Option("Search above, then select a doctor", ""));
                        const term = doctorSearch.value.trim();
                        if (!term) {
                            doctorStatus.textContent = "Nhập từ khóa để tìm bác sĩ đang hoạt động.";
                            return;
                        }
                        doctorStatus.textContent = "Đang tìm bác sĩ…";
                        doctorTimer = window.setTimeout(async () => {
                            doctorRequest = new AbortController();
                            const url = new URL(doctorSearch.dataset.searchUrl, window.location.href);
                            url.searchParams.set("term", term);
                            try {
                                const result = await fetch(url, {
                                    headers: {Accept: "application/json"},
                                    signal: doctorRequest.signal
                                });
                                if (!result.ok) {
                                    throw new Error("Doctor search failed.");
                                }
                                const doctors = await result.json();
                                if (!Array.isArray(doctors)
                                        || doctors.some((doctor) => !Number.isInteger(doctor.id)
                                            || doctor.id <= 0
                                            || typeof doctor.name !== "string"
                                            || typeof doctor.phone !== "string")) {
                                    throw new Error("Doctor search returned invalid results.");
                                }
                                if (doctors.length === 0) {
                                    doctorStatus.textContent = "Không tìm thấy bác sĩ đang hoạt động.";
                                    return;
                                }
                                doctorSelect.size = Math.min(doctors.length + 1, 6);
                                doctors.forEach((doctor) => {
                                    doctorSelect.append(new Option(
                                        doctor.name + " — " + doctor.phone, doctor.id));
                                });
                                doctorStatus.textContent = doctors.length + " matching doctor(s) found.";
                            } catch (error) {
                                if (error.name !== "AbortError") {
                                    doctorStatus.textContent = "Chưa thể tìm bác sĩ. Vui lòng thử lại.";
                                }
                            }
                        }, 250);
                    });
                    doctorSelect.addEventListener("change", () => {
                        if (doctorSelect.value) {
                            doctorSelect.size = 1;
                        }
                    });
                }
            })();
        </script>
    </body>
</html>
