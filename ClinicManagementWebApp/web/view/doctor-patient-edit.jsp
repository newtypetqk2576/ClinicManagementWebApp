<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chỉnh sửa bệnh nhân</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Chỉnh sửa bệnh nhân</h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <dl>
                <dt>Mã bệnh nhân</dt><dd><c:out value="${patient.patientID}"/></dd>
                <dt>Ngày tạo</dt><dd><c:out value="${patient.createdAt}"/></dd>
                <dt>Tài khoản bệnh nhân</dt>
                <dd><c:choose><c:when test="${not empty patient.userID}">Đã liên kết</c:when><c:otherwise>Chưa liên kết</c:otherwise></c:choose></dd>
            </dl>
            <c:url var="patientsUrl" value="/patients"/>
            <form method="post" action="<c:out value="${patientsUrl}"/>">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="<c:out value="${patient.patientID}"/>">
                <label for="fullName">Họ và tên</label>
                <input id="fullName" name="fullName" maxlength="100" required
                       value="<c:out value="${patient.fullName}"/>">
                <label for="dateOfBirth">Ngày sinh</label>
                <input id="dateOfBirth" type="date" name="dateOfBirth"
                       value="<c:out value="${dateOfBirthValue}"/>">
                <label for="gender">Giới tính</label>
                <select id="gender" name="gender" required>
                    <option value="">Chọn giới tính</option>
                    <c:forEach var="genderOption" items="${genderOptions}">
                        <option value="<c:out value="${genderOption}"/>"
                                <c:if test="${patient.gender eq genderOption}">selected</c:if>>
                            <c:out value="${genderOption}"/>
                        </option>
                    </c:forEach>
                </select>
                <label for="phone">Số điện thoại</label>
                <input id="phone" name="phone" maxlength="15" required
                       value="<c:out value="${patient.phone}"/>">
                <label for="email">Email</label>
                <input id="email" type="email" name="email" maxlength="100"
                       value="<c:out value="${patient.email}"/>">
                <label for="address">Địa chỉ</label>
                <textarea id="address" name="address" maxlength="255"><c:out value="${patient.address}"/></textarea>
                <button type="submit">Lưu thay đổi</button>
            </form>
            <c:url var="listUrl" value="/patients"/>
            <p><a href="<c:out value="${listUrl}"/>">Về danh sách bệnh nhân</a></p>
        </main>
    </body>
</html>
