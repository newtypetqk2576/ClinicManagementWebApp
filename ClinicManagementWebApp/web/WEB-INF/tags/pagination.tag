<%@tag pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${not empty listRoute and listTotal gt 0}">
    <nav class="list-pagination" aria-label="Phân trang">
        <span>Trang ${listPage}/${listPages} · ${listTotal} kết quả</span>
        <c:if test="${listPage gt 1}">
            <c:url var="previous" value="${listRoute}">
                <c:param name="q" value="${listQuery}"/>
                <c:param name="date" value="${listDate}"/>
                <c:param name="page" value="${listPage - 1}"/>
            </c:url>
            <a class="button" rel="prev" href="<c:out value="${previous}${listAnchor}"/>">Trang trước</a>
        </c:if>
        <c:if test="${listPage lt listPages}">
            <c:url var="next" value="${listRoute}">
                <c:param name="q" value="${listQuery}"/>
                <c:param name="date" value="${listDate}"/>
                <c:param name="page" value="${listPage + 1}"/>
            </c:url>
            <a class="button" rel="next" href="<c:out value="${next}${listAnchor}"/>">Trang sau</a>
        </c:if>
    </nav>
</c:if>
