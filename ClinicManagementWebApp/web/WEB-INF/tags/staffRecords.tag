<%@tag pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<list:filters/>
<c:choose>
    <c:when test="${empty records}">
        <p>Không tìm thấy hồ sơ khám.</p>
    </c:when>
    <c:otherwise>
        <div class="table-scroll">
            <table>
                <thead>
                    <tr>
                        <th scope="col">Bệnh nhân</th>
                        <th scope="col">Bác sĩ</th>
                        <th scope="col">Ngày giờ khám</th>
                        <th scope="col">Triệu chứng</th>
                        <th scope="col">Trạng thái</th>
                        <th scope="col">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="record" items="${records}">
                        <tr>
                            <td><c:out value="${record.patientName}"/></td>
                            <td><c:out value="${record.doctorName}"/></td>
                            <td><list:dateTime value="${record.visitDate}"/></td>
                            <td><c:out value="${record.symptoms}"/></td>
                            <td><list:status value="${record.status}"/></td>
                            <td>
                                <c:url var="detailUrl" value="${routeBase}">
                                    <c:param name="id" value="${record.medicalRecordID}"/>
                                </c:url>
                                <a href="<c:out value="${detailUrl}"/>">Xem chi tiết</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>
<list:pagination/>
