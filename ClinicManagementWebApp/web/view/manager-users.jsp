<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Tài khoản</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Tài khoản</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:url var="newUserUrl" value="/admin/users/new"/>
            <p><a class="button" href="<c:out value="${newUserUrl}"/>">Tạo tài khoản</a></p>
            <list:filters/>
            <c:choose>
                <c:when test="${empty users}">
                    <p>Không tìm thấy tài khoản.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th scope="col">Tên đăng nhập</th>
                                    <th scope="col">Vai trò</th>
                                    <th scope="col">Trạng thái</th>
                                    <th scope="col">Ngày tạo</th>
                                    <th scope="col">Ngày cập nhật</th>
                                    <th scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="user" items="${users}">
                                    <tr>
                                        <td><c:out value="${user.username}"/></td>
                                        <td><list:status value="${user.role}"/></td>
                                        <td><c:choose><c:when test="${user.active}">Đang hoạt động</c:when><c:otherwise>Ngừng hoạt động</c:otherwise></c:choose></td>
                                        <td><list:dateTime value="${user.createdAt}"/></td>
                                        <td><list:dateTime value="${user.updatedAt}"/></td>
                                        <td>
                                            <c:url var="userDetailUrl" value="/admin/users/detail">
                                                <c:param name="id" value="${user.userID}"/>
                                            </c:url>
                                            <a href="<c:out value="${userDetailUrl}"/>">Xem chi tiết</a>
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
