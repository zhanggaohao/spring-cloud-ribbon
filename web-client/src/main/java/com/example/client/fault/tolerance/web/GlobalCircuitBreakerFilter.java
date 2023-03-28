package com.example.client.fault.tolerance.web;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

/**
 * GlobalCircuitBreakerFilter
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/23
 */
@WebFilter(filterName = "globalCircuitBreakerFilter", urlPatterns = "/*")
public class GlobalCircuitBreakerFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(GlobalCircuitBreakerFilter.class);

    private CircuitBreaker circuitBreaker;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        CircuitBreakerConfig circuitBreakerConfig = new CircuitBreakerConfig.Builder()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(10000))
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(2)
                .minimumNumberOfCalls(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                .build();
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        logger.info("Initialize Circuit Breaker.");
        circuitBreaker = circuitBreakerRegistry.circuitBreaker(filterConfig.getFilterName());


        mBeanServer = ManagementFactory.getPlatformMBeanServer();
        cbMetrics = new CBMetrics();

        Hashtable<String, String> map = new Hashtable<>();
        map.put("type", "CB");
        map.put("id", "state");
        map.put("name", "state");
        try {
            mBeanServer.registerMBean(cbMetrics, ObjectName.getInstance("fault.tolerance", map));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CBMetrics cbMetrics;
    private MBeanServer mBeanServer;

    static class CBMetrics implements DynamicMBean {
        private CircuitBreaker.State state;
//        private CircuitBreaker.Metrics metrics;

        public CBMetrics() {
            this.state = CircuitBreaker.State.CLOSED;
        }

        public CircuitBreaker.State getState() {
            return state;
        }

        public void setState(CircuitBreaker.State state) {
            this.state = state;
        }

//        public CircuitBreaker.Metrics getMetrics() {
//            return metrics;
//        }
//
//        public void setMetrics(CircuitBreaker.Metrics metrics) {
//            this.metrics = metrics;
//        }

        @Override
        public Object getAttribute(String attribute) {
            return state.name();
        }

        @Override
        public void setAttribute(Attribute attribute) {
            this.state = CircuitBreaker.State.valueOf((String) attribute.getValue());
        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            throw new UnsupportedOperationException("setAttributes is not implemented");
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            throw new UnsupportedOperationException("setAttributes is not implemented");
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) {
            throw new UnsupportedOperationException("invoke is not implemented");
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            return new MBeanInfo("circuitBreaker.state", "熔断状态",
                    new MBeanAttributeInfo[]{new MBeanAttributeInfo("value", "java.lang.String", "熔断状态", true, true, false)},
                    null, null, null);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        logger.info("Processing Request.");
        circuitBreaker.acquirePermission();
        long start = System.nanoTime();
        try {
            filterChain.doFilter(servletRequest, servletResponse);
            long durationInNanos = System.nanoTime() - start;
            circuitBreaker.onSuccess(durationInNanos, TimeUnit.NANOSECONDS);
        } catch (Exception exception) {
            // Do not handle java.lang.Error
            long durationInNanos = System.nanoTime() - start;
            circuitBreaker.onError(durationInNanos, TimeUnit.NANOSECONDS, exception);
            throw exception;
        } finally {
            cbMetrics.setState(circuitBreaker.getState());
//            cbMetrics.setMetrics(circuitBreaker.getMetrics());
        }
    }

    @Override
    public void destroy() {
    }
}
