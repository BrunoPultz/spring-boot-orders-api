package com.brunopultz.orderms.service;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

import java.math.BigDecimal;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.brunopultz.orderms.controller.dto.OrderResponse;
import com.brunopultz.orderms.entity.OrderEntity;
import com.brunopultz.orderms.entity.OrderItem;
import com.brunopultz.orderms.listener.dto.OrderCreatedEvent;
import com.brunopultz.orderms.repositotory.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MongoTemplate mongoTemplate;

    public OrderService(OrderRepository orderRepository, MongoTemplate mongoTemplate) {
        this.orderRepository = orderRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void save(OrderCreatedEvent event) {

        var entity = new OrderEntity();

        entity.setOrderId(event.codigoPedido());
        entity.setCustomerId(event.codigoCliente());
        entity.setItems(getOrderItems(event));
        entity.setTotal(getTotal(event));

        orderRepository.save(entity);

    }

    public Page<OrderResponse> findAllByCustomerId(Long customerId, PageRequest pageRequest) {
        var orders = orderRepository.findAllByCustomerId(customerId, pageRequest);

        return orders.map(OrderResponse::fromEntity);

    }

    public BigDecimal findTotalOnOrdersByCustomerId(Long customerId) {
        var aggregation = Aggregation.newAggregation(
                match(Criteria.where("customerId").is(customerId)),
                group().sum("total").as("total"));

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation, "tb_orders", Document.class);

        Document result = results.getUniqueMappedResult();

        if (result != null && result.get("total") != null) {
            return new BigDecimal(result.get("total").toString());
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal getTotal(OrderCreatedEvent event) {
        return event.itens()
                .stream()
                .map(item -> item.preco().multiply(BigDecimal.valueOf(item.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<OrderItem> getOrderItems(OrderCreatedEvent event) {
        return event.itens()
                .stream()
                .map(item -> {
                    var orderItem = new OrderItem();
                    orderItem.setProduct(item.produto());
                    orderItem.setQuantity(item.quantidade());
                    orderItem.setPrice(item.preco());
                    return orderItem;
                })
                .toList();
    }
}
