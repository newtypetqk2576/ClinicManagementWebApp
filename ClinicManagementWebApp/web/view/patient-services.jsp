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
                                    <th scope="col">Dịch vụ khám</th>
                                    <th scope="col">Mô tả</th>
                                    <th scope="col">Giá (₫)</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="service" items="${services}">
                                    <tr>
                                        <td><c:out value="${service.serviceName}"/></td>
                                        <td><c:out value="${service.description}"/></td>
                                        <td><c:out value="${service.price}"/></td>
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
