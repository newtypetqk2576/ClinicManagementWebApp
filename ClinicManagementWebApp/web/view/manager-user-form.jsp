<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Create user</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Create user account</h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:url var="usersUrl" value="/admin/users"/>
            <c:url var="patientSearchUrl" value="/admin/users/search-patients"/>
            <c:url var="doctorSearchUrl" value="/admin/users/search-doctors"/>
            <form method="post" action="<c:out value="${usersUrl}"/>">
                <input type="hidden" name="action" value="create">
                <label for="role">Role</label>
                <select id="role" name="role" required>
                    <option value="">Choose a role</option>
                    <option value="PATIENT" <c:if test="${role eq 'PATIENT'}">selected</c:if>>Patient</option>
                    <option value="DOCTOR" <c:if test="${role eq 'DOCTOR'}">selected</c:if>>Doctor</option>
                    <option value="MANAGER" <c:if test="${role eq 'MANAGER'}">selected</c:if>>Manager</option>
                </select>

                <section id="patientPicker" hidden>
                    <label for="patientSearch">Search patient by name or phone</label>
                    <input id="patientSearch" type="search" autocomplete="off" maxlength="100"
                           value="<c:out value="${patientSearch}"/>"
                           data-search-url="<c:out value="${patientSearchUrl}"/>"
                           aria-describedby="patientSearchStatus">
                    <p id="patientSearchStatus" role="status">Search to see matching patients.</p>
                    <label for="patientID">Patient</label>
                    <select id="patientID" name="patientID" disabled>
                        <option value="">Search above, then select a patient</option>
                        <c:if test="${not empty patientID}">
                            <option value="<c:out value="${patientID}"/>" selected>
                                Previously selected patient — search again if unavailable
                            </option>
                        </c:if>
                    </select>
                </section>

                <section id="doctorPicker" hidden>
                    <label for="doctorSearch">Search doctor by name or phone</label>
                    <input id="doctorSearch" type="search" autocomplete="off" maxlength="100"
                           value="<c:out value="${doctorSearch}"/>"
                           data-search-url="<c:out value="${doctorSearchUrl}"/>"
                           aria-describedby="doctorSearchStatus">
                    <p id="doctorSearchStatus" role="status">Search to see matching doctors.</p>
                    <label for="doctorID">Doctor</label>
                    <select id="doctorID" name="doctorID" disabled>
                        <option value="">Search above, then select a doctor</option>
                        <c:if test="${not empty doctorID}">
                            <option value="<c:out value="${doctorID}"/>" selected>
                                Previously selected doctor — search again if unavailable
                            </option>
                        </c:if>
                    </select>
                </section>

                <label for="username">Username</label>
                <input id="username" name="username" maxlength="50" required
                       value="<c:out value="${username}"/>">
                <label for="password">Password</label>
                <input id="password" name="password" type="text" maxlength="255" required>
                <button type="submit">Create user</button>
            </form>
            <p><a href="<c:out value="${usersUrl}"/>">Back to users</a></p>
        </main>
        <script>
            (() => {
                const role = document.getElementById("role");
                const pickers = {
                    PATIENT: {
                        section: document.getElementById("patientPicker"),
                        input: document.getElementById("patientSearch"),
                        select: document.getElementById("patientID"),
                        status: document.getElementById("patientSearchStatus"),
                        type: "patient"
                    },
                    DOCTOR: {
                        section: document.getElementById("doctorPicker"),
                        input: document.getElementById("doctorSearch"),
                        select: document.getElementById("doctorID"),
                        status: document.getElementById("doctorSearchStatus"),
                        type: "doctor"
                    }
                };

                role.addEventListener("change", () => {
                    Object.entries(pickers).forEach(([key, picker]) => {
                        const active = role.value === key;
                        picker.section.hidden = !active;
                        picker.select.disabled = !active;
                        if (!active) {
                            picker.select.value = "";
                        }
                    });
                });
                role.dispatchEvent(new Event("change"));

                Object.values(pickers).forEach((picker) => {
                    let timer;
                    let activeRequest;
                    picker.input.addEventListener("input", () => {
                        window.clearTimeout(timer);
                        if (activeRequest) {
                            activeRequest.abort();
                        }
                        picker.select.replaceChildren(new Option(
                            "Search above, then select a " + picker.type, ""));
                        const term = picker.input.value.trim();
                        if (!term) {
                            picker.status.textContent = "Search to see matching "
                                + picker.type + "s.";
                            return;
                        }

                        picker.status.textContent = "Searching " + picker.type + "s...";
                        timer = window.setTimeout(async () => {
                            activeRequest = new AbortController();
                            const url = new URL(picker.input.dataset.searchUrl, window.location.href);
                            url.searchParams.set("term", term);
                            try {
                                const result = await fetch(url, {
                                    headers: {Accept: "application/json"},
                                    signal: activeRequest.signal
                                });
                                if (!result.ok) {
                                    throw new Error("Profile search failed.");
                                }
                                const profiles = await result.json();
                                if (!Array.isArray(profiles) || profiles.some((profile) =>
                                    !Number.isInteger(profile.id)
                                    || typeof profile.name !== "string"
                                    || typeof profile.phone !== "string")) {
                                    throw new Error("Profile search returned invalid results.");
                                }
                                profiles.forEach((profile) => {
                                    picker.select.append(new Option(
                                        profile.name + " — " + profile.phone, profile.id));
                                });
                                picker.status.textContent = profiles.length
                                    ? profiles.length + " matching " + picker.type + "(s) found."
                                    : "No matching " + picker.type + "s were found.";
                            } catch (error) {
                                if (error.name !== "AbortError") {
                                    picker.status.textContent = picker.type
                                        + " search is unavailable. Please try again.";
                                }
                            }
                        }, 250);
                    });
                });
            })();
        </script>
    </body>
</html>
