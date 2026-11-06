<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Bác sĩ</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Bác sĩ</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:url var="newDoctorUrl" value="/admin/doctors/new"/>
            <p><a class="button" href="<c:out value="${newDoctorUrl}"/>">Thêm bác sĩ</a></p>
            <list:filters/>
            <c:choose>
                <c:when test="${empty doctors}">
                    <p>Không tìm thấy bác sĩ.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th scope="col">Mã</th>
                                    <th scope="col">Tên</th>
                                    <th scope="col">Chuyên khoa</th>
                                    <th scope="col">Số điện thoại</th>
                                    <th scope="col">Trạng thái</th>
                                    <th scope="col">Tài khoản</th>
                                    <th scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="doctor" items="${doctors}">
                                    <tr>
                                        <td><c:out value="${doctor.doctorID}"/></td>
                                        <td><c:out value="${doctor.fullName}"/></td>
                                        <td><c:out value="${doctor.specialty}"/></td>
                                        <td><c:out value="${doctor.phone}"/></td>
                                        <td><c:choose><c:when test="${doctor.active}">Đang hoạt động</c:when><c:otherwise>Ngừng hoạt động</c:otherwise></c:choose></td>
                                        <td><c:choose><c:when test="${not empty doctor.userID}">Đã liên kết</c:when><c:otherwise>Chưa liên kết</c:otherwise></c:choose></td>
                                        <td>
                                            <c:url var="editDoctorUrl" value="/admin/doctors/edit">
                                                <c:param name="id" value="${doctor.doctorID}"/>
                                            </c:url>
                                            <a href="<c:out value="${editDoctorUrl}"/>">Chỉnh sửa</a>
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
