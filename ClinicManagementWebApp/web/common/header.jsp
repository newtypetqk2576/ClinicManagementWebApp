<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- Pages including this fragment should load /css/common-layout.css in their head. --%>
<header class="app-header">
    <div class="app-shell app-header__inner">
        <a class="app-header__brand" href="<c:url value="/"/>">
            <span class="app-header__mark" aria-hidden="true"></span>
            Clinic Management
        </a>
        <c:choose>
            <c:when test="${not empty sessionScope.Username}">
                <details class="app-user-menu">
                    <summary><c:out value="${sessionScope.Username}"/></summary>
                    <div class="app-user-menu__items">
                        <a href="<c:url value="/account/change-password"/>">Đổi mật khẩu</a>
                        <form method="post" action="<c:url value="/logout"/>">
                            <button type="submit">Đăng xuất</button>
                        </form>
                    </div>
                </details>
            </c:when>
            <c:when test="${param.showGuestActions eq 'true'}">
                <nav class="app-header__actions" aria-label="Tài khoản">
                    <a class="button" href="<c:url value="/login"/>">Đăng nhập</a>
                    <a class="button" href="<c:url value="/register"/>">Tạo tài khoản</a>
                </nav>
            </c:when>
        </c:choose>
    </div>
</header>
