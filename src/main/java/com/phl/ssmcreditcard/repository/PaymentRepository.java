package com.phl.ssmcreditcard.repository;

import com.phl.ssmcreditcard.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
