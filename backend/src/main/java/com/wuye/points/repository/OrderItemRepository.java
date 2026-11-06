package com.wuye.points.repository;

import com.wuye.points.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单商品 Repository
 * 注：order_id 已隐含 propertyCompanyId
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrderId(String orderId);

    /**
     * 某住户「待支付」订单已为该商品预占的件数（整改清单条目13）。
     * 预支付即扣库存，取消支付后订单仍待支付、库存仍被自己的单占着，
     * 试算需要把这部分还回去，否则重进支付页会误判「商品库存不足」。
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN Order o ON o.id = oi.orderId "
            + "WHERE o.residentId = :residentId AND o.orderStatus = 'pending' AND oi.productId = :productId")
    int sumPendingReservedQuantity(@Param("residentId") String residentId,
                                   @Param("productId") String productId);
}
