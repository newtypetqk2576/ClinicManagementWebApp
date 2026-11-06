<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Hồ sơ khám của tôi</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Hồ sơ khám của tôi</h1>
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
                                    <th scope="col">Bác sĩ</th>
                                    <th scope="col">Ngày giờ khám</th>
                                    <th scope="col">Chẩn đoán</th>
                                    <th scope="col">Trạng thái</th>
                                    <th scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="record" items="${records}">
                                    <tr>
                                        <td><c:out value="${record.doctorName}"/></td>
                                        <td><list:dateTime value="${record.visitDate}"/></td>
                                        <td><c:out value="${record.diagnosis}"/></td>
                                        <td><list:status value="${record.status}"/></td>
                                        <td>
                                            <c:url var="recordUrl" value="/medical-records">
                                                <c:param name="id" value="${record.medicalRecordID}"/>
                                            </c:url>
                                            <a href="<c:out value="${recordUrl}"/>">Xem chi tiết</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
            <list:pagination/>
        </main>
    </body>
</html>
