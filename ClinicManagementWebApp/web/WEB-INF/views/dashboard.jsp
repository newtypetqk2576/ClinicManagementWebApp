<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Tổng quan quản lý | Clinic Management</title>
        <link rel="stylesheet" href="<c:url value="/css/common-layout.css"/>">
        <link rel="stylesheet" href="<c:url value="/css/dashboard.css"/>">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <fmt:setLocale value="vi_VN"/>
        <main class="dashboard">
            <header>
                <p class="eyebrow">Quản lý phòng khám</p>
                <h1>Tổng quan</h1>
            </header>
            <div class="dashboard-totals">
                <section class="dashboard-total" aria-labelledby="today-revenue">
                    <p class="eyebrow"><c:out value="${todayLabel}"/></p>
                    <h2 id="today-revenue">Doanh thu hôm nay</h2>
                    <p class="dashboard-total__value"><fmt:formatNumber value="${todayRevenue}" maxFractionDigits="2"/> ₫</p>
                </section>
                <section class="dashboard-total" aria-labelledby="week-revenue">
                    <p class="eyebrow"><c:out value="${weekStart}"/> – <c:out value="${weekEnd}"/></p>
                    <h2 id="week-revenue">Doanh thu tuần này</h2>
                    <p class="dashboard-total__value"><fmt:formatNumber value="${weekRevenue}" maxFractionDigits="2"/> ₫</p>
                </section>
            </div>

            <section aria-labelledby="chart-title">
                <h2 id="chart-title">Doanh thu theo ngày</h2>
                <p class="dashboard-note">Chỉ tính hóa đơn đã thanh toán. Hóa đơn chưa lưu thời điểm thanh toán được tính theo ngày lập.</p>
                <div class="dashboard-chart-scroll">
                    <ol class="dashboard-chart" aria-label="Doanh thu từng ngày từ thứ Hai đến Chủ nhật">
                        <c:forEach var="day" items="${revenueChart}">
                            <li class="dashboard-chart__day ${day.today ? 'dashboard-chart__day--today' : ''}">
                                <strong><fmt:formatNumber value="${day.total}" maxFractionDigits="2"/> ₫</strong>
                                <div class="dashboard-chart__track" aria-hidden="true">
                                    <div class="dashboard-chart__bar" style="--bar-height: ${day.height}%"></div>
                                </div>
                                <span><c:out value="${day.weekday}"/> · <c:out value="${day.date}"/></span>
                            </li>
                        </c:forEach>
                    </ol>
                </div>
            </section>

            <section aria-labelledby="today-records">
                <h2 id="today-records">Hồ sơ khám hôm nay</h2>
                <list:staffRecords/>
            </section>
        </main>
    </body>
</html>
