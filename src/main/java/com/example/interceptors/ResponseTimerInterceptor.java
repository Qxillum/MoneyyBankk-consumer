package com.example.interceptors;

import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class ResponseTimerInterceptor implements WriterInterceptor {
    private static final Logger LOG = Logger.getLogger(ResponseTimerInterceptor.class.getName());
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException {
        long t0 = System.currentTimeMillis();
        context.proceed();
        LOG.info(() -> "⏱ Temps de génération de la réponse: " + (System.currentTimeMillis()-t0) + " ms");
    }
}
