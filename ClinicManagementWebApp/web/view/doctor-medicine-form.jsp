<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:choose><c:when test="${createMode}">Create medicine</c:when><c:otherwise>Edit medicine</c:otherwise></c:choose></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1><c:choose><c:when test="${createMode}">Create medicine</c:when><c:otherwise>Edit medicine</c:otherwise></c:choose></h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:if test="${not createMode}">
                <dl>
                    <dt>Medicine ID</dt><dd><c:out value="${medicine.medicineID}"/></dd>
                    <dt>Created at</dt><dd><c:out value="${medicine.createdAt}"/></dd>
                </dl>
            </c:if>
            <c:url var="medicinesUrl" value="/doctor/medicines"/>
            <form method="post" action="<c:out value="${medicinesUrl}"/>">
                <input type="hidden" name="action" value="${createMode ? 'create' : 'update'}">
                <c:if test="${not createMode}">
                    <input type="hidden" name="id" value="<c:out value="${medicine.medicineID}"/>">
                </c:if>
                <label for="medicineName">Medicine name</label>
                <input id="medicineName" name="medicineName" maxlength="150" required
                       value="<c:out value="${medicine.medicineName}"/>">
                <label for="activeIngredient">Active ingredient</label>
                <input id="activeIngredient" name="activeIngredient" maxlength="150"
                       value="<c:out value="${medicine.activeIngredient}"/>">
                <label for="unit">Unit</label>
                <input id="unit" name="unit" maxlength="30" required
                       value="<c:out value="${medicine.unit}"/>">
                <label for="price">Price</label>
                <input id="price" type="number" name="price" min="0" max="9999999999.99" step="0.01" required
                       value="<c:out value="${priceValue}"/>">
                <label for="stockQuantity">Stock quantity</label>
                <input id="stockQuantity" type="number" name="stockQuantity" min="0" step="1" required
                       value="<c:out value="${stockValue}"/>">
                <label>
                    <input type="checkbox" name="isActive" value="true" ${medicine.active ? 'checked' : ''}>
                    Active
                </label>
                <button type="submit"><c:choose><c:when test="${createMode}">Create medicine</c:when><c:otherwise>Save changes</c:otherwise></c:choose></button>
            </form>
            <p><a href="<c:out value="${medicinesUrl}"/>">Back to medicines</a></p>
        </main>
    </body>
</html>
