package com.cfg.kitchen.printer;

import com.cfg.order.domain.Order;

/**
 * Abstraction pour l'impression thermique future.
 * Implémentation courante: no-op. Remplacer par ESC/POS ou autre sans toucher au reste.
 */
public interface PrinterService {
    void printOrderTicket(Order order);
    void printReceipt(Order order);
    boolean isAvailable();
}
