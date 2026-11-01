package Controller;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter({"/account/*", "/auth/change-password.jsp", "/view/*"})
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String servletPath = httpRequest.getServletPath();
        boolean doctorView = servletPath.startsWith("/view/doctor-");
        boolean protectedAccountRoute = servletPath.startsWith("/account/")
                || "/auth/change-password.jsp".equals(servletPath);
        if (!doctorView && !protectedAccountRoute) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean authenticated = session != null
                && session.getAttribute("UserID") instanceof Integer
                && session.getAttribute("Username") instanceof String;
        if (!authenticated) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }
        Object role = session.getAttribute("Role");
        if (!("PATIENT".equals(role) || "DOCTOR".equals(role) || "MANAGER".equals(role))) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (doctorView && !"DOCTOR".equals(role)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}
