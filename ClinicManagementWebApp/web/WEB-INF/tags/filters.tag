<%@tag pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${not empty listRoute}">
    <c:url var="filterUrl" value="${listRoute}"/>
    <form class="list-filters" method="get" action="<c:out value="${filterUrl}${listAnchor}"/>" aria-label="Bộ lọc danh sách">
        <label>
            <c:out value="${listNameLabel}"/>
            <input type="search" name="q" value="<c:out value="${listQuery}"/>">
        </label>
        <c:if test="${not empty listDateLabel}">
            <label>
                <c:out value="${listDateLabel}"/>
                <input type="date" name="date" value="<c:out value="${listDate}"/>">
            </label>
        </c:if>
        <button type="submit">Lọc</button>
        <c:if test="${not empty listQuery or not empty listDate}">
            <a href="<c:out value="${filterUrl}${listAnchor}"/>">Xóa bộ lọc</a>
        </c:if>
    </form>
</c:if>
