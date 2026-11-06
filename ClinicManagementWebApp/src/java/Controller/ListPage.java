package Controller;

import jakarta.servlet.http.HttpServletRequest;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

final class ListPage {
    private static final int PAGE_SIZE = 5;
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");

    private ListPage() {
    }

    static <T> void byName(HttpServletRequest request, String attribute, List<T> rows,
            String nameLabel, Function<T, String> nameOf) {
        filter(request, attribute, rows, nameLabel, nameOf, null, null, PAGE_SIZE);
    }

    static <T> void allByName(HttpServletRequest request, String attribute, List<T> rows,
            String nameLabel, Function<T, String> nameOf) {
        filter(request, attribute, rows, nameLabel, nameOf, null, null, Math.max(1, rows.size()));
    }

    static <T> void byNameAndDate(HttpServletRequest request, String attribute, List<T> rows,
            String nameLabel, Function<T, String> nameOf, String dateLabel,
            Function<T, LocalDateTime> dateOf) {
        filter(request, attribute, rows, nameLabel, nameOf, dateLabel, dateOf, PAGE_SIZE);
    }

    private static <T> void filter(HttpServletRequest request, String attribute, List<T> rows,
            String nameLabel, Function<T, String> nameOf, String dateLabel,
            Function<T, LocalDateTime> dateOf, int pageSize) {
        String query = request.getParameter("q");
        query = query == null ? "" : query.strip();
        String needle = normalize(query);
        LocalDate date = dateLabel == null ? null : parseDate(request.getParameter("date"));
        List<T> matches = rows.stream()
                .filter(row -> needle.isEmpty() || normalize(nameOf.apply(row)).contains(needle))
                .filter(row -> date == null || date.equals(dateOf.apply(row).toLocalDate()))
                .toList();
        int pages = Math.max(1, (matches.size() + pageSize - 1) / pageSize);
        int page = Math.min(readPage(request.getParameter("page")), pages);
        int start = (page - 1) * pageSize;
        request.setAttribute(attribute, matches.subList(start, Math.min(start + pageSize, matches.size())));
        request.setAttribute("listRoute", request.getServletPath());
        request.setAttribute("listNameLabel", nameLabel);
        request.setAttribute("listDateLabel", dateLabel);
        request.setAttribute("listQuery", query);
        request.setAttribute("listDate", date == null ? "" : date.toString());
        request.setAttribute("listPage", page);
        request.setAttribute("listPages", pages);
        request.setAttribute("listTotal", matches.size());
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT).replace('đ', 'd');
        return MARKS.matcher(Normalizer.normalize(lower, Normalizer.Form.NFD)).replaceAll("");
    }

    private static LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static int readPage(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}
