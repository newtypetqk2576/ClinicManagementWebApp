<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:choose><c:when test="${createMode}">Thêm dịch vụ khám</c:when><c:otherwise>Chỉnh sửa dịch vụ khám</c:otherwise></c:choose></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1><c:choose><c:when test="${createMode}">Thêm dịch vụ khám</c:when><c:otherwise>Chỉnh sửa dịch vụ khám</c:otherwise></c:choose></h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:if test="${not createMode}">
                <dl>
                    <dt>Mã dịch vụ</dt><dd><c:out value="${service.serviceID}"/></dd>
                    <dt>Ngày tạo</dt><dd><c:out value="${service.createdAt}"/></dd>
                </dl>
            </c:if>
            <c:url var="servicesUrl" value="/doctor/services"/>
            <form method="post" action="<c:out value="${servicesUrl}"/>">
                <input type="hidden" name="action" value="${createMode ? 'create' : 'update'}">
                <c:if test="${not createMode}">
                    <input type="hidden" name="id" value="<c:out value="${service.serviceID}"/>">
                </c:if>
                <label for="serviceName">Tên dịch vụ</label>
                <input id="serviceName" name="serviceName" maxlength="150" required
                       value="<c:out value="${service.serviceName}"/>">
                <label for="description">Mô tả</label>
                <textarea id="description" name="description" maxlength="500"><c:out value="${service.description}"/></textarea>
                <label for="price">Giá (₫)</label>
                <input id="price" type="number" name="price" min="0" max="9999999999.99" step="0.01" required
                       value="<c:out value="${priceValue}"/>">
                <label>
                    <input type="checkbox" name="isActive" value="true" ${service.active ? 'checked' : ''}>
                    Active
                </label>
                <button type="submit"><c:choose><c:when test="${createMode}">Thêm dịch vụ khám</c:when><c:otherwise>Lưu thay đổi</c:otherwise></c:choose></button>
            </form>
            <p><a href="<c:out value="${servicesUrl}"/>">Về danh mục dịch vụ khám</a></p>
        </main>
    </body>
</html>
