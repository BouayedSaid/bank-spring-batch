package org.id.bankspringbatch.dao;

import org.id.bankspringbatch.entities.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
}
