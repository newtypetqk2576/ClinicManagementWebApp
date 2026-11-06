<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chi tiết hồ sơ khám</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Chi tiết hồ sơ khám</h1>
            <c:url var="recordsUrl" value="/medical-records"/>
            <p><a href="<c:out value="${recordsUrl}"/>">Về danh sách hồ sơ khám</a></p>

            <section aria-labelledby="record-heading">
                <h2 id="record-heading">Hồ sơ khám</h2>
                <dl>
                    <dt>Mã hồ sơ khám</dt><dd><c:out value="${record.medicalRecordID}"/></dd>
                    <dt>Bác sĩ</dt><dd><c:out value="${record.doctorName}"/></dd>
                    <dt>Ngày giờ khám</dt><dd><list:dateTime value="${record.visitDate}"/></dd>
                    <dt>Chẩn đoán</dt><dd><c:out value="${record.diagnosis}"/></dd>
                    <dt>Triệu chứng</dt><dd><c:out value="${record.symptoms}"/></dd>
                    <dt>Ghi chú điều trị</dt><dd><c:out value="${record.treatmentNotes}"/></dd>
                    <dt>Trạng thái</dt><dd><list:status value="${record.status}"/></dd>
                    <dt>Ngày tạo</dt><dd><list:dateTime value="${record.createdAt}"/></dd>
                </dl>
            </section>

            <section aria-labelledby="services-heading">
                <h2 id="services-heading">Dịch vụ khám</h2>
                <c:choose>
                    <c:when test="${empty record.services}">
                        <p>Hồ sơ này chưa có dịch vụ khám.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-scroll">
                            <table>
                                <thead>
                                    <tr>
                                        <th scope="col">Dịch vụ khám</th>
                                        <th scope="col">Số lượng</th>
                                        <th scope="col">Đơn giá</th>
                                        <th scope="col">Ghi chú</th>
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
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section aria-labelledby="prescriptions-heading">
                <h2 id="prescriptions-heading">Đơn thuốc</h2>
                <c:choose>
                    <c:when test="${empty record.prescriptions}">
                        <p>Hồ sơ này chưa có đơn thuốc.</p>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="prescription" items="${record.prescriptions}">
                            <article>
                                <h3>Prescription <c:out value="${prescription.prescriptionID}"/></h3>
                                <dl>
                                    <dt>Ngày</dt><dd><list:dateTime value="${prescription.prescriptionDate}"/></dd>
                                    <dt>Trạng thái</dt><dd><list:status value="${prescription.status}"/></dd>
                                    <dt>Ghi chú</dt><dd><c:out value="${prescription.notes}"/></dd>
                                </dl>
                                <c:choose>
                                    <c:when test="${empty prescription.medicines}">
                                        <p>Đơn thuốc này chưa có thuốc.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="table-scroll">
                                            <table>
                                                <thead>
                                                    <tr>
                                                        <th scope="col">Thuốc</th>
                                                        <th scope="col">Liều dùng</th>
                                                        <th scope="col">Tần suất</th>
                                                        <th scope="col">Số ngày dùng</th>
                                                        <th scope="col">Số lượng</th>
                                                        <th scope="col">Đơn giá</th>
                                                        <th scope="col">Hướng dẫn</th>
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
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </article>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </section>

            <section aria-labelledby="invoice-heading">
                <h2 id="invoice-heading">Hóa đơn</h2>
                <c:choose>
                    <c:when test="${empty record.invoice}">
                        <p>Hồ sơ này chưa có hóa đơn.</p>
                    </c:when>
                    <c:otherwise>
                        <dl>
                            <dt>Mã hóa đơn</dt><dd><c:out value="${record.invoice.invoiceID}"/></dd>
                            <dt>Ngày</dt><dd><list:dateTime value="${record.invoice.invoiceDate}"/></dd>
                            <dt>Tiền dịch vụ</dt><dd><c:out value="${record.invoice.serviceAmount}"/></dd>
                            <dt>Tiền thuốc</dt><dd><c:out value="${record.invoice.medicineAmount}"/></dd>
                            <dt>Giảm giá</dt><dd><c:out value="${record.invoice.discountAmount}"/></dd>
                            <dt>Tổng tiền</dt><dd><c:out value="${record.invoice.totalAmount}"/></dd>
                            <dt>Phương thức thanh toán</dt><dd><list:status value="${record.invoice.paymentMethod}"/></dd>
                            <dt>Trạng thái thanh toán</dt><dd><list:status value="${record.invoice.paymentStatus}"/></dd>
                            <dt>Ghi chú</dt><dd><c:out value="${record.invoice.notes}"/></dd>
                        </dl>
                    </c:otherwise>
                </c:choose>
            </section>
        </main>
    </body>
</html>
