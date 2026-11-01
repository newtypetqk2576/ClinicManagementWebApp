<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Clinic services</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Active services</h1>
            <c:choose>
                <c:when test="${empty services}">
                    <p>There are no active services available.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">Service</th>
                                <th scope="col">Description</th>
                                <th scope="col">Price</th>
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
                </c:otherwise>
            </c:choose>
        </main>
    </body>
</html>
