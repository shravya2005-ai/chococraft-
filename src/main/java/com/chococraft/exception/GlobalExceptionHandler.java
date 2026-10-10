package com.chococraft.exception;

import com.chococraft.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        return uri.startsWith("/api/") || (acceptHeader != null && acceptHeader.contains("application/json"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.error("Resource not found error: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/shop";
    }

    @ExceptionHandler(InsufficientStockException.class)
    public Object handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.error("Stock error: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
        }
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/cart";
    }

    @ExceptionHandler(BadRequestException.class)
    public Object handleBadRequest(BadRequestException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.error("Bad request error: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
        }
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        logger.error("Validation error: {}", errorMessage);
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorMessage));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneralException(Exception ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.error("Unhandled exception: ", ex);
        String message = "An unexpected error occurred. Please try again later.";
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(message));
        }
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:/";
    }
}
