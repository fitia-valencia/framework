package com.monframework.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import com.monframework.web.annotation.RestController;
import com.monframework.web.annotation.GetMapping;
import org.reflections.Reflections;

public class DispatcherServlet extends HttpServlet {
    private Object helloControllerInstance;
    private Method helloMethod;

    @Override
    public void init() {
        // Scan du package com.testapp.controllers pour trouver HelloController
        try {
            Reflections reflections = new Reflections("com.testapp.controllers");
            Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(RestController.class);
            for (Class<?> controller : controllers) {
                for (Method m : controller.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(GetMapping.class)) {
                        String path = m.getAnnotation(GetMapping.class).value();
                        if ("/hello".equals(path)) {
                            helloControllerInstance = controller.getDeclaredConstructor().newInstance();
                            helloMethod = m;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String context = req.getContextPath();
        String path = uri.substring(context.length());
        if ("/hello".equals(path) && helloControllerInstance != null && helloMethod != null) {
            try {
                Object result = helloMethod.invoke(helloControllerInstance);
                resp.setContentType("text/plain");
                resp.getWriter().write(result.toString());
            } catch (Exception e) {
                resp.setStatus(500);
                resp.getWriter().write("Erreur interne: " + e.getMessage());
            }
        } else {
            resp.setStatus(404);
            resp.getWriter().write("Not found: " + path);
        }
    }
}