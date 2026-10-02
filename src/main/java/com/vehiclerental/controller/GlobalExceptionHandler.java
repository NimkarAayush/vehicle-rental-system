package com.vehiclerental.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "File too large! Maximum allowed size is 5MB.");
        // We guess that they came from admin/vehicles/new or edit, best fallback is list
        return "redirect:/admin/vehicles"; 
    }
    
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException exc, RedirectAttributes redirectAttributes) {
        if (exc.getMessage() != null && exc.getMessage().contains("JPG and PNG")) {
            redirectAttributes.addFlashAttribute("errorMessage", exc.getMessage());
            return "redirect:/admin/vehicles"; 
        }
        throw exc;
    }
}
