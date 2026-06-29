package com.cfg.kitchen.printer;

import com.cfg.order.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NoOpPrinterService implements PrinterService {

    @Override
    public void printOrderTicket(Order order) {
        log.debug("NoOp: printOrderTicket for order {}", order.getId());
    }

    @Override
    public void printReceipt(Order order) {
        log.debug("NoOp: printReceipt for order {}", order.getId());
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
