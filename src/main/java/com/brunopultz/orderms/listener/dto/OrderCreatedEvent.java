package com.brunopultz.orderms.listener.dto;

import java.util.List;

public record OrderCreatedEvent(Long codigoPedido,
        Long codigoCliente,
        List<OrderItemEvent> itens) {

}
