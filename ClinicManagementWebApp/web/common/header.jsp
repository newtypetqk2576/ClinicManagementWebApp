<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- Pages including this fragment should load /css/common-layout.css in their head. --%>
<header class="app-header">
    <c:if test="${not empty sessionScope.Username}">
        <details class="app-user-menu">
            <summary><c:out value="${sessionScope.Username}"/></summary>
            <div class="app-user-menu__items">
                <c:url var="changePasswordUrl" value="/account/change-password"/>
                <a href="<c:out value="${changePasswordUrl}"/>">Change password</a>
                <c:url var="logoutUrl" value="/logout"/>
                <form method="post" action="<c:out value="${logoutUrl}"/>">
                    <button type="submit">Sign out</button>
                </form>
            </div>
        </details>
    </c:if>
    <a class="app-header__brand" href="${pageContext.request.contextPath}/" aria-label="Clinic Management home">
        Clinic Management
    </a>
</header>
