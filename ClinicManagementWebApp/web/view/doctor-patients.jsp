<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Bệnh nhân</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Bệnh nhân</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>

            <h2>Thêm bệnh nhân</h2>
            <c:url var="patientsUrl" value="/patients"/>
            <form method="post" action="<c:out value="${patientsUrl}"/>">
                <input type="hidden" name="action" value="create">
                <label for="fullName">Họ và tên</label>
                <input id="fullName" name="fullName" maxlength="100" required
                       value="<c:out value="${formPatient.fullName}"/>">
                <label for="dateOfBirth">Ngày sinh</label>
                <input id="dateOfBirth" type="date" name="dateOfBirth"
                       value="<c:out value="${dateOfBirthValue}"/>">
                <label for="gender">Giới tính</label>
                <select id="gender" name="gender" required>
                    <option value="">Chọn giới tính</option>
                    <c:forEach var="genderOption" items="${genderOptions}">
                        <option value="<c:out value="${genderOption}"/>"
                                <c:if test="${formPatient.gender eq genderOption}">selected</c:if>>
                            <c:out value="${genderOption}"/>
                        </option>
                    </c:forEach>
                </select>
                <label for="phone">Số điện thoại</label>
                <input id="phone" name="phone" maxlength="15" required
                       value="<c:out value="${formPatient.phone}"/>">
                <label for="email">Email</label>
                <input id="email" type="email" name="email" maxlength="100"
                       value="<c:out value="${formPatient.email}"/>">
                <label for="address">Địa chỉ</label>
                <textarea id="address" name="address" maxlength="255"><c:out value="${formPatient.address}"/></textarea>
                <button type="submit">Thêm bệnh nhân</button>
            </form>

            <h2>Danh sách bệnh nhân</h2>
            <list:filters/>
            <c:choose>
                <c:when test="${empty patients}">
                    <p>Không tìm thấy bệnh nhân.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr><th scope="col">Tên</th><th scope="col">Số điện thoại</th><th scope="col">Thao tác</th></tr>
                            </thead>
                            <tbody>
                                <c:forEach var="patient" items="${patients}">
                                    <tr>
                                        <td><c:out value="${patient.fullName}"/></td>
                                        <td><c:out value="${patient.phone}"/></td>
                                        <td>
                                            <c:url var="editUrl" value="/patients">
                                                <c:param name="id" value="${patient.patientID}"/>
                                            </c:url>
                                            <a href="<c:out value="${editUrl}"/>">Chỉnh sửa</a>
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
