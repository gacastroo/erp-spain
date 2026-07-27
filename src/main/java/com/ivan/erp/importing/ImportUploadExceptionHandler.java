package com.ivan.erp.importing;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ImportUploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleFileTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String viewName = requestUri != null && requestUri.startsWith("/products/")
                ? "products/import"
                : "clients/import";

        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("errorMessage", "El archivo supera el límite de 5 MB.");
        return modelAndView;
    }
}
