<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Medical record details</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Medical record details</h1>
            <c:url var="recordsUrl" value="/medical-records"/>
            <p><a href="<c:out value="${recordsUrl}"/>">Back to medical records</a></p>

            <section aria-labelledby="record-heading">
                <h2 id="record-heading">Record</h2>
                <dl>
                    <dt>Record ID</dt><dd><c:out value="${record.medicalRecordID}"/></dd>
                    <dt>Doctor</dt><dd><c:out value="${record.doctorName}"/></dd>
                    <dt>VisitDate</dt><dd><c:out value="${record.visitDate}"/></dd>
                    <dt>Diagnosis</dt><dd><c:out value="${record.diagnosis}"/></dd>
                    <dt>Symptoms</dt><dd><c:out value="${record.symptoms}"/></dd>
                    <dt>Treatment notes</dt><dd><c:out value="${record.treatmentNotes}"/></dd>
                    <dt>Status</dt><dd><c:out value="${record.status}"/></dd>
                    <dt>CreatedAt</dt><dd><c:out value="${record.createdAt}"/></dd>
                </dl>
            </section>

            <section aria-labelledby="services-heading">
                <h2 id="services-heading">Services</h2>
                <c:choose>
                    <c:when test="${empty record.services}">
                        <p>No services are listed for this record.</p>
                    </c:when>
                    <c:otherwise>
                        <table>
                            <thead>
                                <tr>
                                    <th scope="col">Service</th>
                                    <th scope="col">Quantity</th>
                                    <th scope="col">Unit price</th>
                                    <th scope="col">Notes</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="service" items="${record.services}">
                                    <tr>
                                        <td><c:out value="${service.serviceName}"/></td>
                                        <td><c:out value="${service.quantity}"/></td>
                                        <td><c:out value="${service.unitPrice}"/></td>
                                        <td><c:out value="${service.notes}"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </section>

            <section aria-labelledby="prescriptions-heading">
                <h2 id="prescriptions-heading">Prescriptions</h2>
                <c:choose>
                    <c:when test="${empty record.prescriptions}">
                        <p>No prescriptions are listed for this record.</p>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="prescription" items="${record.prescriptions}">
                            <article>
                                <h3>Prescription <c:out value="${prescription.prescriptionID}"/></h3>
                                <dl>
                                    <dt>Date</dt><dd><c:out value="${prescription.prescriptionDate}"/></dd>
                                    <dt>Status</dt><dd><c:out value="${prescription.status}"/></dd>
                                    <dt>Notes</dt><dd><c:out value="${prescription.notes}"/></dd>
                                </dl>
                                <c:choose>
                                    <c:when test="${empty prescription.medicines}">
                                        <p>No medicines are listed in this prescription.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <table>
                                            <thead>
                                                <tr>
                                                    <th scope="col">Medicine</th>
                                                    <th scope="col">Dosage</th>
                                                    <th scope="col">Frequency</th>
                                                    <th scope="col">Duration (days)</th>
                                                    <th scope="col">Quantity</th>
                                                    <th scope="col">Unit price</th>
                                                    <th scope="col">Instructions</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="medicine" items="${prescription.medicines}">
                                                    <tr>
                                                        <td>
                                                            <c:out value="${medicine.medicineName}"/>
                                                            <c:if test="${not empty medicine.activeIngredient}">
                                                                (<c:out value="${medicine.activeIngredient}"/>)
                                                            </c:if>
                                                            <c:if test="${not empty medicine.unit}">
                                                                — <c:out value="${medicine.unit}"/>
                                                            </c:if>
                                                        </td>
                                                        <td><c:out value="${medicine.dosage}"/></td>
                                                        <td><c:out value="${medicine.frequency}"/></td>
                                                        <td><c:out value="${medicine.durationDays}"/></td>
                                                        <td><c:out value="${medicine.quantity}"/></td>
                                                        <td><c:out value="${medicine.unitPrice}"/></td>
                                                        <td><c:out value="${medicine.instructions}"/></td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </c:otherwise>
                                </c:choose>
                            </article>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </section>

            <section aria-labelledby="invoice-heading">
                <h2 id="invoice-heading">Invoice</h2>
                <c:choose>
                    <c:when test="${empty record.invoice}">
                        <p>No invoice has been issued for this record.</p>
                    </c:when>
                    <c:otherwise>
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
                    </c:otherwise>
                </c:choose>
            </section>
        </main>
    </body>
</html>
