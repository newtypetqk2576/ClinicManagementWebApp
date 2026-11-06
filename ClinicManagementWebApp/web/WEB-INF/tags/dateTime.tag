<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@attribute name="value" required="true" type="java.lang.String"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:if test="${not empty value}">
    <time datetime="<c:out value="${value}"/>">${fn:substring(value, 8, 10)}/${fn:substring(value, 5, 7)}/${fn:substring(value, 0, 4)} ${fn:substring(value, 11, 16)}</time>
</c:if>
