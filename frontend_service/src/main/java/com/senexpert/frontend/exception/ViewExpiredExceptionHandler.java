package com.senexpert.frontend.exception;

import jakarta.faces.FacesException;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialResponseWriter;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Iterator;

/**
 * Gère ViewExpiredException :
 * – AJAX  → envoie un redirect JSF partiel vers la même URL
 * – HTTP  → redirect standard vers la même URL (ou login si inconnue)
 */
public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {

    public ViewExpiredExceptionHandler(ExceptionHandler wrapped) {
        super(wrapped);
    }

    @Override
    public void handle() throws FacesException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) { super.handle(); return; }

        Iterator<ExceptionQueuedEvent> it = getUnhandledExceptionQueuedEvents().iterator();
        while (it.hasNext()) {
            ExceptionQueuedEvent event = it.next();
            ExceptionQueuedEventContext eCtx = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = eCtx.getException();

            if (t instanceof ViewExpiredException) {
                ExternalContext ext = ctx.getExternalContext();
                try {
                    String redirectUrl = buildRedirectUrl(ext);

                    if (ctx.getPartialViewContext().isAjaxRequest()) {
                        // Réponse partielle JSF pour AJAX
                        ext.setResponseContentType("text/xml");
                        ext.setResponseCharacterEncoding("UTF-8");
                        ext.addResponseHeader("Cache-Control", "no-cache");
                        PartialResponseWriter writer = ctx.getPartialViewContext().getPartialResponseWriter();
                        writer.startDocument();
                        writer.redirect(redirectUrl);
                        writer.endDocument();
                        ctx.responseComplete();
                    } else {
                        ext.redirect(redirectUrl);
                    }
                } catch (IOException ex) {
                    throw new FacesException(ex);
                } finally {
                    it.remove();
                }
                break;
            }
        }
        super.handle();
    }

    private String buildRedirectUrl(ExternalContext ext) {
        HttpServletRequest req = (HttpServletRequest) ext.getRequest();
        String uri = req.getRequestURI();
        String qs  = req.getQueryString();
        if (qs != null && !qs.isBlank()) {
            return uri + "?" + qs;
        }
        return uri;
    }
}
