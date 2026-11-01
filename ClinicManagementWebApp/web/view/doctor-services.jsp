<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Services</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Services</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:url var="newServiceUrl" value="/doctor/services/new"/>
            <p><a href="<c:out value="${newServiceUrl}"/>">Create service</a></p>
            <c:choose>
                <c:when test="${empty services}">
                    <p>No services were found.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">ID</th>
                                <th scope="col">Name</th>
                                <th scope="col">Description</th>
                                <th scope="col">Price</th>
                                <th scope="col">Status</th>
                                <th scope="col">Created at</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="service" items="${services}">
                                <tr>
                                    <td><c:out value="${service.serviceID}"/></td>
                                    <td><c:out value="${service.serviceName}"/></td>
                                    <td><c:out value="${service.description}"/></td>
                                    <td><c:out value="${service.price}"/></td>
                                    <td><c:choose><c:when test="${service.active}">Active</c:when><c:otherwise>Inactive</c:otherwise></c:choose></td>
                                    <td><c:out value="${service.createdAt}"/></td>
                                    <td>
                                        <c:url var="editServiceUrl" value="/doctor/services/edit">
                                            <c:param name="id" value="${service.serviceID}"/>
                                        </c:url>
                                        <a href="<c:out value="${editServiceUrl}"/>">Edit</a>
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
