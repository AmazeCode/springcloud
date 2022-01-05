package com.amazecode.eureka.provider.service;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Service;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/4 17:30
 */
@Service
public class HealthService implements HealthIndicator {

    private Boolean status = true;

    public String getStatus() {
        return status.toString();
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    @Override
    public Health health() {
        if (status) {
            // 标记上线
            return new Health.Builder().up().build();
        }
        // 设置下线
        return new Health.Builder().down().build();
    }
}
