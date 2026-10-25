package io.github.buildblocks.apiresponse.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

class RequestIdFilterOrderTest {

    @Test
    void runsBeforeSpringSecurityFilterChain() {
        Integer order = OrderUtils.getOrder(RequestIdFilter.class);

        assertEquals(Integer.valueOf(Ordered.HIGHEST_PRECEDENCE), order);
    }
}