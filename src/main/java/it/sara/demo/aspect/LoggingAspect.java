package it.sara.demo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AOP aspect for cross-cutting logging.
 * <ul>
 *   <li><b>Controllers</b> (INFO): logs HTTP method + URI on entry, confirms response on exit.</li>
 *   <li><b>Services</b> (DEBUG): logs method name on entry and exit with elapsed time.</li>
 * </ul>
 * Internal step-level debug logs are written inline inside each method.
 * Error logging remains in {@code GlobalExceptionHandler} and catch blocks.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* it.sara.demo.web..*Controller.*(..))")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest httpRequest = currentHttpRequest();
        String httpMethod = httpRequest != null ? httpRequest.getMethod() : "?";
        String uri = httpRequest != null ? httpRequest.getRequestURI() : pjp.getSignature().getName();

        log.info("Request - [{} {}]", httpMethod, uri);
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        log.info("Response - [{} {}] - [{}ms]", httpMethod, uri, System.currentTimeMillis() - start);
        return result;
    }

    @Around("execution(* it.sara.demo.service..*ServiceImpl.*(..))")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        log.debug("Starting method - {}", method);
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        log.debug("Ending method - {} [{}ms]", method, System.currentTimeMillis() - start);
        return result;
    }

    private HttpServletRequest currentHttpRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attrs.getRequest();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}