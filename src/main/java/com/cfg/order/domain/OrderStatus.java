package com.cfg.order.domain;

public enum OrderStatus {
    DRAFT,          // En cours de construction côté serveur
    PENDING,        // Envoyé en cuisine, en attente
    PREPARING,      // En préparation
    READY,          // Prêt à servir
    SERVED,         // Servi à table
    PAID,           // Payé et clôturé
    CANCELLED       // Annulé
}
