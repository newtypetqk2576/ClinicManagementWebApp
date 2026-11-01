<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Medicines</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Medicines</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:url var="newMedicineUrl" value="/doctor/medicines/new"/>
            <p><a href="<c:out value="${newMedicineUrl}"/>">Create medicine</a></p>
            <c:choose>
                <c:when test="${empty medicines}">
                    <p>No medicines were found.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">ID</th>
                                <th scope="col">Name</th>
                                <th scope="col">Active ingredient</th>
                                <th scope="col">Unit</th>
                                <th scope="col">Price</th>
                                <th scope="col">Stock quantity</th>
                                <th scope="col">Status</th>
                                <th scope="col">Created at</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="medicine" items="${medicines}">
                                <tr>
                                    <td><c:out value="${medicine.medicineID}"/></td>
                                    <td><c:out value="${medicine.medicineName}"/></td>
                                    <td><c:out value="${medicine.activeIngredient}"/></td>
                                    <td><c:out value="${medicine.unit}"/></td>
                                    <td><c:out value="${medicine.price}"/></td>
                                    <td><c:out value="${medicine.stockQuantity}"/></td>
                                    <td><c:choose><c:when test="${medicine.active}">Active</c:when><c:otherwise>Inactive</c:otherwise></c:choose></td>
                                    <td><c:out value="${medicine.createdAt}"/></td>
                                    <td>
                                        <c:url var="editMedicineUrl" value="/doctor/medicines/edit">
                                            <c:param name="id" value="${medicine.medicineID}"/>
                                        </c:url>
                                        <a href="<c:out value="${editMedicineUrl}"/>">Edit</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </main>
    </body>
</html>
