package com.transferencia.repositories;

import com.transferencia.entities.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferenciaRepository extends JpaRepository<Transferencia, String> {
}
