package com.API.API_limiter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 36)
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanType plan;

    public enum PlanType {
        FREE(10, 1),
        GOLD(50, 5);

        private final int limit;
        private final int refillRate;

        PlanType(int limit, int refillRate) {
            this.limit = limit;
            this.refillRate = refillRate;
        }

        public int getLimit() {
            return limit;
        }

        public int getRefillRate() {
            return refillRate;
        }

        public static PlanType from(String rawPlan) {
            return PlanType.valueOf(rawPlan.trim().toUpperCase());
        }
    }
}
