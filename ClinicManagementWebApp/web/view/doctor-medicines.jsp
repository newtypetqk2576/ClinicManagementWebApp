<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Thuốc</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Thuốc</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:url var="newMedicineUrl" value="/doctor/medicines/new"/>
            <p><a class="button" href="<c:out value="${newMedicineUrl}"/>">Thêm thuốc</a></p>
            <list:filters/>
            <c:choose>
                <c:when test="${empty medicines}">
                    <p>Không tìm thấy thuốc.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th scope="col">Mã</th>
                                    <th scope="col">Tên</th>
                                    <th scope="col">Hoạt chất</th>
                                    <th scope="col">Đơn vị</th>
                                    <th scope="col">Giá (₫)</th>
                                    <th scope="col">Tồn kho</th>
                                    <th scope="col">Trạng thái</th>
                                    <th scope="col">Ngày tạo</th>
                                    <th scope="col">Thao tác</th>
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
                                        <td><c:choose><c:when test="${medicine.active}">Đang hoạt động</c:when><c:otherwise>Ngừng hoạt động</c:otherwise></c:choose></td>
                                        <td><list:dateTime value="${medicine.createdAt}"/></td>
                                        <td>
                                            <c:url var="editMedicineUrl" value="/doctor/medicines/edit">
                                                <c:param name="id" value="${medicine.medicineID}"/>
                                            </c:url>
                                            <a href="<c:out value="${editMedicineUrl}"/>">Chỉnh sửa</a>
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
