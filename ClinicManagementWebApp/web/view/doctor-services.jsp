<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Dịch vụ khám</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Dịch vụ khám</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:url var="newServiceUrl" value="/doctor/services/new"/>
            <p><a class="button" href="<c:out value="${newServiceUrl}"/>">Thêm dịch vụ khám</a></p>
            <list:filters/>
            <c:choose>
                <c:when test="${empty services}">
                    <p>Không tìm thấy dịch vụ khám.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th scope="col">Mã</th>
                                    <th scope="col">Tên</th>
                                    <th scope="col">Mô tả</th>
                                    <th scope="col">Giá (₫)</th>
                                    <th scope="col">Trạng thái</th>
                                    <th scope="col">Ngày tạo</th>
                                    <th scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="service" items="${services}">
                                    <tr>
                                        <td><c:out value="${service.serviceID}"/></td>
                                        <td><c:out value="${service.serviceName}"/></td>
                                        <td><c:out value="${service.description}"/></td>
                                        <td><c:out value="${service.price}"/></td>
                                        <td><c:choose><c:when test="${service.active}">Đang hoạt động</c:when><c:otherwise>Ngừng hoạt động</c:otherwise></c:choose></td>
                                        <td><list:dateTime value="${service.createdAt}"/></td>
                                        <td>
                                            <c:url var="editServiceUrl" value="/doctor/services/edit">
                                                <c:param name="id" value="${service.serviceID}"/>
                                            </c:url>
                                            <a href="<c:out value="${editServiceUrl}"/>">Chỉnh sửa</a>
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
