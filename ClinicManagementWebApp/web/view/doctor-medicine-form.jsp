<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:choose><c:when test="${createMode}">Thêm thuốc</c:when><c:otherwise>Chỉnh sửa thuốc</c:otherwise></c:choose></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1><c:choose><c:when test="${createMode}">Thêm thuốc</c:when><c:otherwise>Chỉnh sửa thuốc</c:otherwise></c:choose></h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:if test="${not createMode}">
                <dl>
                    <dt>Mã thuốc</dt><dd><c:out value="${medicine.medicineID}"/></dd>
                    <dt>Ngày tạo</dt><dd><c:out value="${medicine.createdAt}"/></dd>
                </dl>
            </c:if>
            <c:url var="medicinesUrl" value="/doctor/medicines"/>
            <form method="post" action="<c:out value="${medicinesUrl}"/>">
                <input type="hidden" name="action" value="${createMode ? 'create' : 'update'}">
                <c:if test="${not createMode}">
                    <input type="hidden" name="id" value="<c:out value="${medicine.medicineID}"/>">
                </c:if>
                <label for="medicineName">Tên thuốc</label>
                <input id="medicineName" name="medicineName" maxlength="150" required
                       value="<c:out value="${medicine.medicineName}"/>">
                <label for="activeIngredient">Hoạt chất</label>
                <input id="activeIngredient" name="activeIngredient" maxlength="150"
                       value="<c:out value="${medicine.activeIngredient}"/>">
                <label for="unit">Đơn vị</label>
                <input id="unit" name="unit" maxlength="30" required
                       value="<c:out value="${medicine.unit}"/>">
                <label for="price">Giá (₫)</label>
                <input id="price" type="number" name="price" min="0" max="9999999999.99" step="0.01" required
                       value="<c:out value="${priceValue}"/>">
                <label for="stockQuantity">Tồn kho</label>
                <input id="stockQuantity" type="number" name="stockQuantity" min="0" step="1" required
                       value="<c:out value="${stockValue}"/>">
                <label>
                    <input type="checkbox" name="isActive" value="true" ${medicine.active ? 'checked' : ''}>
                    Active
                </label>
                <button type="submit"><c:choose><c:when test="${createMode}">Thêm thuốc</c:when><c:otherwise>Lưu thay đổi</c:otherwise></c:choose></button>
            </form>
            <p><a href="<c:out value="${medicinesUrl}"/>">Về danh mục thuốc</a></p>
        </main>
    </body>
</html>
