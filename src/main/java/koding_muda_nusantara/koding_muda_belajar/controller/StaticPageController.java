package koding_muda_nusantara.koding_muda_belajar.controller;

import jakarta.servlet.http.HttpSession;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.CourseService;
import koding_muda_nusantara.koding_muda_belajar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller untuk halaman-halaman statis
 */
@Controller
public class StaticPageController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CourseService courseService;

    @GetMapping("/about")
    public String aboutPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        // Statistik untuk halaman about
        long totalStudents = userService.getTotalStudents();
        long totalLecturers = userService.getTotalLecturers();
        long totalCourses = courseService.getTotalCourses();
        
        model.addAttribute("user", user);
        if (user != null) {
            model.addAttribute("role", userService.getUserRole(user));
        }
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalLecturers", totalLecturers);
        model.addAttribute("totalCourses", totalCourses);
        
        return "about";
    }
    
    @GetMapping("/tentang")
    public String tentangPage(Model model, HttpSession session) {
        // Redirect ke /about
        return aboutPage(model, session);
    }
}
