package koding_muda_nusantara.koding_muda_belajar.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom Error Controller untuk menangani error pages
 * Mengimplementasikan ErrorController agar menggantikan BasicErrorController bawaan Spring Boot
 */
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == 404) {
                return "error/404";
            } else if (statusCode == 403) {
                return "error/403";
            } else if (statusCode == 500) {
                return "error/500";
            }
        }
        return "error/500";
    }
}
