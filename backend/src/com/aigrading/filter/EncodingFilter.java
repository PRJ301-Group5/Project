package com.aigrading.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Filter ensuring UTF-8 encoding across all HTTP requests and responses.
 * Essential for Vietnamese Unicode support.
 */
@WebFilter(filterName = "EncodingFilter", urlPatterns = {"/*"})
public class EncodingFilter implements Filter {
    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String enc = filterConfig.getInitParameter("encoding");
        if (enc != null && !enc.trim().isEmpty()) {
            this.encoding = enc;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
