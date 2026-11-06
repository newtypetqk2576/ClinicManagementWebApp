<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Dịch vụ khám | Clinic Management</title>
        <link rel="stylesheet" href="<c:url value="/css/common-layout.css"/>">
        <link rel="stylesheet" href="<c:url value="/css/home.css"/>">
    </head>
    <body>
        <jsp:include page="/common/header.jsp">
            <jsp:param name="showGuestActions" value="true"/>
        </jsp:include>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main class="app-shell home">
            <section class="home-hero" aria-labelledby="home-title">
                <div class="home-hero__content">
                    <p class="eyebrow">Cổng thông tin phòng khám</p>
                    <h1 id="home-title">Tìm dịch vụ khám<br class="home-hero__break"> phù hợp với bạn</h1>
                    <p class="home-hero__description">Xem thông tin và chi phí của từng dịch vụ. Đăng nhập tài khoản bệnh nhân để đặt lịch và theo dõi hồ sơ khám.</p>
                    <a class="button" href="#dich-vu">Xem dịch vụ khám <span aria-hidden="true">↓</span></a>
                </div>
                <div class="home-hero__art" aria-hidden="true">
                    <svg viewBox="0 0 400 236" width="400" height="236" fill="none" focusable="false">
                        <circle cx="200" cy="118" r="112" stroke="#cdd2d3" stroke-dasharray="3 6"/>
                        <circle cx="200" cy="118" r="91" stroke="#cdd2d3"/>
                        <path d="M12 119h87l14-16 12 16h150l12-16 13 16h88" stroke="#a9b7b8" stroke-width="2"/>
                        <rect x="144" y="62" width="112" height="112" rx="30" fill="white" stroke="#d3d9da"/>
                        <path d="M200 89v58m-29-29h58" stroke="#007b73" stroke-width="14" stroke-linecap="round"/>
                        <circle cx="113" cy="103" r="4" fill="#007b73"/>
                        <circle cx="287" cy="103" r="4" fill="#007b73"/>
                        <path d="M73 54v14m-7-7h14m247 105v14m-7-7h14" stroke="#a9b7b8" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
            </section>

            <section class="home-catalog" id="dich-vu" aria-labelledby="catalog-title">
                <header class="home-catalog__heading">
                    <p class="eyebrow">Danh mục phòng khám</p>
                    <h2 id="catalog-title">Dịch vụ khám</h2>
                    <p>Thông tin và chi phí các dịch vụ hiện có tại phòng khám.</p>
                </header>
                <list:filters/>
                <c:choose>
                    <c:when test="${catalogUnavailable}">
                        <p class="home-catalog__notice" role="status">Hiện chưa thể tải danh sách dịch vụ. Vui lòng quay lại sau.</p>
                    </c:when>
                    <c:when test="${empty services}">
                        <p class="home-catalog__notice">Không tìm thấy dịch vụ khám.</p>
                    </c:when>
                    <c:otherwise>
                        <fmt:setLocale value="vi_VN"/>
                        <ul class="home-services">
                            <c:forEach var="service" items="${services}">
                                <li class="home-service">
                                    <h3><c:out value="${service.serviceName}"/></h3>
                                    <c:if test="${not empty service.description}">
                                        <p><c:out value="${service.description}"/></p>
                                    </c:if>
                                    <p class="home-service__price">
                                        <fmt:formatNumber value="${service.price}" maxFractionDigits="2"/> <span>₫</span>
                                    </p>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </section>
        </main>
    </body>
</html>
