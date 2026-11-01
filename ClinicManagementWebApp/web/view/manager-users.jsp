<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Users</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Users</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:url var="newUserUrl" value="/admin/users/new"/>
            <p><a href="<c:out value="${newUserUrl}"/>">Create user account</a></p>
            <c:choose>
                <c:when test="${empty users}">
                    <p>No users were found.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">Username</th>
                                <th scope="col">Role</th>
                                <th scope="col">Status</th>
                                <th scope="col">Created at</th>
                                <th scope="col">Updated at</th>
                                <th scope="col">Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="user" items="${users}">
                                <tr>
                                    <td><c:out value="${user.username}"/></td>
                                    <td><c:out value="${user.role}"/></td>
                                    <td><c:choose><c:when test="${user.active}">Active</c:when><c:otherwise>Inactive</c:otherwise></c:choose></td>
                                    <td><c:out value="${user.createdAt}"/></td>
                                    <td><c:out value="${user.updatedAt}"/></td>
                                    <td>
                                        <c:url var="userDetailUrl" value="/admin/users/detail">
                                            <c:param name="id" value="${user.userID}"/>
                                        </c:url>
                                        <a href="<c:out value="${userDetailUrl}"/>">View details</a>
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
