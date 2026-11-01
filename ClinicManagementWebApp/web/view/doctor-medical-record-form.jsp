<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:choose><c:when test="${createMode}">Create medical record</c:when><c:otherwise>Edit medical record</c:otherwise></c:choose></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1><c:choose><c:when test="${createMode}">Create medical record</c:when><c:otherwise>Medical record detail / edit</c:otherwise></c:choose></h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:url var="recordsUrl" value="/doctor/medical-records"/>
            <c:url var="formAction" value="/doctor/medical-records"/>
            <c:url var="patientSearchUrl" value="/doctor/medical-records/search-patients"/>
            <c:url var="serviceSearchUrl" value="/doctor/medical-records/search-services"/>
            <c:url var="medicineSearchUrl" value="/doctor/medical-records/search-medicines"/>

            <c:if test="${not createMode}">
                <dl>
                    <dt>Record ID</dt><dd><c:out value="${record.medicalRecordID}"/></dd>
                    <dt>Visit date</dt><dd><c:out value="${record.visitDate}"/></dd>
                    <dt>Created at</dt><dd><c:out value="${record.createdAt}"/></dd>
                </dl>
            </c:if>

            <form method="post" action="<c:out value="${formAction}"/>">
                <c:choose>
                    <c:when test="${createMode}">
                        <input type="hidden" name="action" value="create">
                        <c:if test="${not empty appointment}">
                            <input type="hidden" name="appointmentID"
                                   value="<c:out value="${appointment.appointmentID}"/>">
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="id" value="<c:out value="${record.medicalRecordID}"/>">
                    </c:otherwise>
                </c:choose>

                <section aria-labelledby="people-heading">
                    <h2 id="people-heading">Patient and doctor</h2>
                    <c:choose>
                        <c:when test="${not empty appointment}">
                            <dl>
                                <dt>Patient</dt><dd><c:out value="${appointment.patient.fullName}"/></dd>
                                <dt>Patient phone</dt><dd><c:out value="${appointment.patient.phone}"/></dd>
                                <dt>Appointment</dt><dd><c:out value="${appointment.appointmentDate}"/> — <c:out value="${appointment.reason}"/></dd>
                            </dl>
                        </c:when>
                        <c:when test="${createMode}">
                            <label for="patientSearch">Search patient by name or phone</label>
                            <p>The current database schema allows only one record without an appointment; link additional records to an appointment.</p>
                            <input id="patientSearch" type="search" autocomplete="off"
                                   data-search-url="<c:out value="${patientSearchUrl}"/>">
                            <p id="patientSearchStatus" role="status">Search to see matching patients.</p>
                            <label for="patientResults">Select patient</label>
                            <select id="patientResults" size="1">
                                <option value="">Search above, then select a patient</option>
                                <c:if test="${not empty patientIDValue}">
                                    <option value="<c:out value="${patientIDValue}"/>" selected>
                                        <c:out value="${patientNameValue}"/> — <c:out value="${patientPhoneValue}"/>
                                    </option>
                                </c:if>
                            </select>
                            <input id="patientID" type="hidden" name="patientID"
                                   value="<c:out value="${patientIDValue}"/>">
                            <input id="patientName" type="hidden" name="patientName"
                                   value="<c:out value="${patientNameValue}"/>">
                            <input id="patientPhone" type="hidden" name="patientPhone"
                                   value="<c:out value="${patientPhoneValue}"/>">
                        </c:when>
                        <c:otherwise>
                            <dl>
                                <dt>Patient</dt><dd><c:out value="${record.patientName}"/></dd>
                                <dt>Doctor</dt><dd><c:out value="${record.doctorName}"/></dd>
                            </dl>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${createMode}">
                        <p>Doctor: <c:out value="${doctor.fullName}"/>
                            <c:if test="${not empty doctor.phone}"> — <c:out value="${doctor.phone}"/></c:if>
                            (signed-in Doctor)
                        </p>
                    </c:if>
                </section>

                <section aria-labelledby="record-heading">
                    <h2 id="record-heading">Clinical record</h2>
                    <label for="symptoms">Symptoms</label>
                    <textarea id="symptoms" name="symptoms" maxlength="1000"><c:out value="${record.symptoms}"/></textarea>
                    <label for="diagnosis">Diagnosis</label>
                    <textarea id="diagnosis" name="diagnosis" maxlength="1000"><c:out value="${record.diagnosis}"/></textarea>
                    <label for="treatmentNotes">Treatment notes</label>
                    <textarea id="treatmentNotes" name="treatmentNotes" maxlength="2000"><c:out value="${record.treatmentNotes}"/></textarea>
                    <label for="status">Status</label>
                    <select id="status" name="status" required>
                        <option value="IN_PROGRESS" <c:if test="${record.status eq 'IN_PROGRESS'}">selected</c:if>>In progress</option>
                        <option value="COMPLETED" <c:if test="${record.status eq 'COMPLETED'}">selected</c:if>>Completed</option>
                        <option value="CANCELLED" <c:if test="${record.status eq 'CANCELLED'}">selected</c:if>>Cancelled</option>
                    </select>
                </section>

                <section aria-labelledby="services-heading">
                    <h2 id="services-heading">Services</h2>
                    <c:if test="${itemsEditable}">
                        <label for="serviceSearch">Search services by name</label>
                        <input id="serviceSearch" type="search" autocomplete="off"
                               data-search-url="<c:out value="${serviceSearchUrl}"/>">
                        <p id="serviceSearchStatus" role="status">Search to see active services.</p>
                        <label for="serviceResults">Select service</label>
                        <select id="serviceResults" size="1"><option value="">Search above, then select a service</option></select>
                        <button id="addService" type="button">Add service</button>
                    </c:if>
                    <div id="serviceLines">
                        <c:forEach var="service" items="${record.services}">
                            <fieldset class="record-line">
                                <legend><c:out value="${service.serviceName}"/> — unit price <c:out value="${service.unitPrice}"/></legend>
                                <input type="hidden" name="serviceID" value="<c:out value="${service.serviceID}"/>">
                                <label>Quantity
                                    <input type="number" name="serviceQuantity" min="1" value="<c:out value="${service.quantity}"/>"
                                           <c:if test="${not itemsEditable}">readonly</c:if>>
                                </label>
                                <label>Notes
                                    <input type="text" name="serviceNotes" maxlength="500" value="<c:out value="${service.notes}"/>"
                                           <c:if test="${not itemsEditable}">readonly</c:if>>
                                </label>
                                <c:if test="${itemsEditable}"><button type="button" class="remove-line">Remove service</button></c:if>
                            </fieldset>
                        </c:forEach>
                    </div>
                </section>

                <section aria-labelledby="medicine-heading">
                    <h2 id="medicine-heading">Prescription and medicines</h2>
                    <c:if test="${itemsEditable}">
                        <label for="medicineSearch">Search medicines by name</label>
                        <input id="medicineSearch" type="search" autocomplete="off"
                               data-search-url="<c:out value="${medicineSearchUrl}"/>">
                        <p id="medicineSearchStatus" role="status">Search to see active medicines.</p>
                        <label for="medicineResults">Select medicine</label>
                        <select id="medicineResults" size="1"><option value="">Search above, then select a medicine</option></select>
                        <button id="addMedicine" type="button">Add medicine</button>
                    </c:if>
                    <label for="prescriptionNotes">Prescription notes</label>
                    <textarea id="prescriptionNotes" name="prescriptionNotes" maxlength="1000"
                              <c:if test="${not itemsEditable}">readonly</c:if>><c:if test="${not empty record.prescriptions}"><c:out value="${record.prescriptions[0].notes}"/></c:if></textarea>
                    <div id="medicineLines">
                        <c:forEach var="prescription" items="${record.prescriptions}">
                            <c:forEach var="medicine" items="${prescription.medicines}">
                                <fieldset class="record-line">
                                    <legend><c:out value="${medicine.medicineName}"/>
                                        <c:if test="${not empty medicine.unit}"> — <c:out value="${medicine.unit}"/></c:if>
                                        — unit price <c:out value="${medicine.unitPrice}"/>
                                    </legend>
                                    <input type="hidden" name="medicineID" value="<c:out value="${medicine.medicineID}"/>">
                                    <label>Quantity
                                        <input type="number" name="medicineQuantity" min="1" value="<c:out value="${medicine.quantity}"/>"
                                               <c:if test="${not itemsEditable}">readonly</c:if>>
                                    </label>
                                    <label>Dosage
                                        <input type="text" name="dosage" maxlength="100" value="<c:out value="${medicine.dosage}"/>"
                                               <c:if test="${not itemsEditable}">readonly</c:if>>
                                    </label>
                                    <label>Frequency
                                        <input type="text" name="frequency" maxlength="200" value="<c:out value="${medicine.frequency}"/>"
                                               <c:if test="${not itemsEditable}">readonly</c:if>>
                                    </label>
                                    <label>Duration (days)
                                        <input type="number" name="durationDays" min="1" value="<c:out value="${medicine.durationDays}"/>"
                                               <c:if test="${not itemsEditable}">readonly</c:if>>
                                    </label>
                                    <label>Instructions
                                        <input type="text" name="instructions" maxlength="500" value="<c:out value="${medicine.instructions}"/>"
                                               <c:if test="${not itemsEditable}">readonly</c:if>>
                                    </label>
                                    <c:if test="${itemsEditable}"><button type="button" class="remove-line">Remove medicine</button></c:if>
                                </fieldset>
                            </c:forEach>
                        </c:forEach>
                    </div>
                    <c:if test="${not itemsEditable}">
                        <p>Items cannot be changed after an invoice exists or a prescription has been issued or cancelled.</p>
                    </c:if>
                </section>

                <section aria-labelledby="invoice-heading">
                    <h2 id="invoice-heading">Invoice</h2>
                    <c:choose>
                        <c:when test="${not empty record.invoice}">
                            <dl>
                                <dt>Invoice ID</dt><dd><c:out value="${record.invoice.invoiceID}"/></dd>
                                <dt>Date</dt><dd><c:out value="${record.invoice.invoiceDate}"/></dd>
                                <dt>Service amount</dt><dd><c:out value="${record.invoice.serviceAmount}"/></dd>
                                <dt>Medicine amount</dt><dd><c:out value="${record.invoice.medicineAmount}"/></dd>
                                <dt>Discount</dt><dd><c:out value="${record.invoice.discountAmount}"/></dd>
                                <dt>Total</dt><dd><c:out value="${record.invoice.totalAmount}"/></dd>
                                <dt>Payment method</dt><dd><c:out value="${record.invoice.paymentMethod}"/></dd>
                                <dt>Payment status</dt><dd><c:out value="${record.invoice.paymentStatus}"/></dd>
                                <dt>Notes</dt><dd><c:out value="${record.invoice.notes}"/></dd>
                            </dl>
                        </c:when>
                        <c:otherwise>
                            <p>No invoice exists for this record. If selected, the server calculates the total from saved service and medicine prices; payment status starts as UNPAID.</p>
                            <label>
                                <input type="checkbox" name="createInvoice" value="true">
                                Create invoice
                            </label>
                        </c:otherwise>
                    </c:choose>
                </section>

                <button type="submit"><c:choose><c:when test="${createMode}">Create medical record</c:when><c:otherwise>Save changes</c:otherwise></c:choose></button>
            </form>
            <p><a href="<c:out value="${recordsUrl}"/>">Back to medical records</a></p>
        </main>
        <c:if test="${createMode or itemsEditable}">
            <script>
                (() => {
                    const patientSearch = document.getElementById("patientSearch");
                    if (patientSearch) {
                        const results = document.getElementById("patientResults");
                        const status = document.getElementById("patientSearchStatus");
                        const patientID = document.getElementById("patientID");
                        const patientName = document.getElementById("patientName");
                        const patientPhone = document.getElementById("patientPhone");
                        let timer;
                        let activeRequest;
                        patientSearch.addEventListener("input", () => {
                            window.clearTimeout(timer);
                            if (activeRequest) {
                                activeRequest.abort();
                            }
                            results.replaceChildren(new Option("Search above, then select a patient", ""));
                            patientID.value = "";
                            patientName.value = "";
                            patientPhone.value = "";
                            const term = patientSearch.value.trim();
                            if (!term) {
                                status.textContent = "Search to see matching patients.";
                                return;
                            }
                            status.textContent = "Searching patients...";
                            timer = window.setTimeout(async () => {
                                activeRequest = new AbortController();
                                const url = new URL(patientSearch.dataset.searchUrl, window.location.href);
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
                                            || patients.some((item) => !Number.isInteger(item.id)
                                                || item.id <= 0 || typeof item.name !== "string"
                                                || typeof item.phone !== "string")) {
                                        throw new Error("Invalid patient search response.");
                                    }
                                    patients.forEach((patient) => {
                                        const option = new Option(patient.name + " — " + patient.phone, patient.id);
                                        option.dataset.name = patient.name;
                                        option.dataset.phone = patient.phone;
                                        results.append(option);
                                    });
                                    status.textContent = patients.length
                                        ? patients.length + " matching patient(s) found."
                                        : "No matching patients were found.";
                                } catch (error) {
                                    if (error.name !== "AbortError") {
                                        status.textContent = "Patient search is unavailable. Please try again.";
                                    }
                                }
                            }, 250);
                        });
                        results.addEventListener("change", () => {
                            const selected = results.selectedOptions[0];
                            patientID.value = selected && selected.value ? selected.value : "";
                            patientName.value = selected ? selected.dataset.name || "" : "";
                            patientPhone.value = selected ? selected.dataset.phone || "" : "";
                        });
                    }

                    function wireItemSearch(config) {
                        const input = document.getElementById(config.input);
                        if (!input) {
                            return;
                        }
                        const results = document.getElementById(config.results);
                        const status = document.getElementById(config.status);
                        const list = document.getElementById(config.list);
                        let timer;
                        let activeRequest;
                        let items = [];
                        input.addEventListener("input", () => {
                            window.clearTimeout(timer);
                            if (activeRequest) {
                                activeRequest.abort();
                            }
                            items = [];
                            results.replaceChildren(new Option(config.prompt, ""));
                            const term = input.value.trim();
                            if (!term) {
                                status.textContent = config.empty;
                                return;
                            }
                            status.textContent = "Searching...";
                            timer = window.setTimeout(async () => {
                                activeRequest = new AbortController();
                                const url = new URL(input.dataset.searchUrl, window.location.href);
                                url.searchParams.set("term", term);
                                try {
                                    const result = await fetch(url, {
                                        headers: {Accept: "application/json"},
                                        signal: activeRequest.signal
                                    });
                                    if (!result.ok) {
                                        throw new Error("Item search failed.");
                                    }
                                    const found = await result.json();
                                    if (!Array.isArray(found)
                                            || found.some((item) => !Number.isInteger(item.id)
                                                || item.id <= 0 || typeof item.name !== "string"
                                                || typeof item.price !== "string")) {
                                        throw new Error("Invalid item search response.");
                                    }
                                    items = found;
                                    found.forEach((item, index) => {
                                        const option = new Option(
                                            item.name + " — " + item.price + config.suffix(item), index);
                                        results.append(option);
                                    });
                                    status.textContent = found.length
                                        ? found.length + " matching item(s) found."
                                        : config.none;
                                } catch (error) {
                                    if (error.name !== "AbortError") {
                                        status.textContent = config.failure;
                                    }
                                }
                            }, 250);
                        });
                        document.getElementById(config.add).addEventListener("click", () => {
                            if (!results.value) {
                                status.textContent = config.select;
                                return;
                            }
                            const item = items[Number(results.value)];
                            if (!item) {
                                status.textContent = config.select;
                                return;
                            }
                            list.append(config.createRow(item));
                            status.textContent = item.name + " added.";
                        });
                    }

                    function field(label, name, value, type, maxLength) {
                        const wrapper = document.createElement("label");
                        wrapper.append(document.createTextNode(label + " "));
                        const input = document.createElement("input");
                        input.type = type || "text";
                        input.name = name;
                        input.value = value || "";
                        if (type === "number") {
                            input.min = "1";
                        }
                        if (maxLength) {
                            input.maxLength = maxLength;
                        }
                        wrapper.append(input);
                        return wrapper;
                    }

                    function createLine(item, settings) {
                        const row = document.createElement("fieldset");
                        row.className = "record-line";
                        const legend = document.createElement("legend");
                        legend.textContent = item.name + (item.unit ? " — " + item.unit : "")
                            + " — unit price " + item.price;
                        row.append(legend);
                        const id = document.createElement("input");
                        id.type = "hidden";
                        id.name = settings.idName;
                        id.value = item.id;
                        row.append(id);
                        row.append(field("Quantity", settings.quantityName, "1", "number"));
                        if (settings.medicine) {
                            row.append(field("Dosage", "dosage", "", "text", 100));
                            row.append(field("Frequency", "frequency", "", "text", 200));
                            row.append(field("Duration (days)", "durationDays", "", "number"));
                            row.append(field("Instructions", "instructions", "", "text", 500));
                        } else {
                            row.append(field("Notes", "serviceNotes", "", "text", 500));
                        }
                        const remove = document.createElement("button");
                        remove.type = "button";
                        remove.className = "remove-line";
                        remove.textContent = "Remove";
                        remove.addEventListener("click", () => row.remove());
                        row.append(remove);
                        return row;
                    }

                    document.querySelectorAll(".remove-line").forEach((button) => {
                        button.addEventListener("click", () => button.closest(".record-line").remove());
                    });
                    wireItemSearch({
                        input: "serviceSearch",
                        results: "serviceResults",
                        status: "serviceSearchStatus",
                        list: "serviceLines",
                        add: "addService",
                        idName: "serviceID",
                        quantityName: "serviceQuantity",
                        prompt: "Search above, then select a service",
                        empty: "Search to see active services.",
                        none: "No matching services were found.",
                        failure: "Service search is unavailable. Please try again.",
                        select: "Select a service first.",
                        suffix: () => "",
                        createRow: (item) => createLine(item, {
                            idName: "serviceID", quantityName: "serviceQuantity"
                        })
                    });
                    wireItemSearch({
                        input: "medicineSearch",
                        results: "medicineResults",
                        status: "medicineSearchStatus",
                        list: "medicineLines",
                        add: "addMedicine",
                        idName: "medicineID",
                        quantityName: "medicineQuantity",
                        prompt: "Search above, then select a medicine",
                        empty: "Search to see active medicines.",
                        none: "No matching medicines were found.",
                        failure: "Medicine search is unavailable. Please try again.",
                        select: "Select a medicine first.",
                        suffix: (item) => item.unit ? " — " + item.unit : "",
                        createRow: (item) => createLine(item, {
                            idName: "medicineID", quantityName: "medicineQuantity", medicine: true
                        })
                    });
                })();
            </script>
        </c:if>
    </body>
</html>
