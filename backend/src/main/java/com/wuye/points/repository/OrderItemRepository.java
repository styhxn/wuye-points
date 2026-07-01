package com.wuye.points.repository;

import com.wuye.points.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单商品 Repository
 * 注：order_id 已隐含 propertyCompanyId
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrderId(String orderId);
}
