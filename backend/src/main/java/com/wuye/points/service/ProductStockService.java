package com.wuye.points.service;

import com.wuye.points.entity.OrderItem;
import com.wuye.points.entity.Product;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.OrderItemRepository;
import com.wuye.points.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品库存的扣减与恢复。
 *
 * <p>把原先散落在 {@code OrderService}（2 处）、{@code PendingPaymentScheduler}、
 * {@code MerchantVerificationService} 的 4 份近乎相同的库存操作收敛为唯一实现，
 * 并统一加上并发保护。
 *
 * <p><b>并发模型</b>：库存是典型的读-改-写争用资源。原先四处均为
 * 「findById → 判断 → setStock → save」，无锁，并发下单会超卖。
 * 此处统一改为 {@code findByIdForUpdate} 行锁，配合 {@code Product} 的
 * {@code @Version} 乐观锁形成双保险。
 *
 * <p><b>事务模型</b>：所有方法使用 {@code Propagation.MANDATORY}——
 * 库存变更必须与调用方（下单/取消/关单）在同一事务内原子提交，
 * 禁止被单独调用而脱离业务事务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStockService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 扣减库存。
     *
     * @throws BizException 商品不存在（{@code PRODUCT_NOT_FOUND}）
     *                      或库存不足（{@code ORDER_STATUS_INVALID}）
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void deduct(String productId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        int stock = product.getStock() != null ? product.getStock() : 0;
        if (stock < quantity) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "商品库存不足");
        }
        int remaining = stock - quantity;
        product.setStock(remaining);
        if (remaining <= 0) {
            product.setStatus("sold_out");
        }
        productRepository.save(product);
    }

    /** 批量扣减（下单预占） */
    @Transactional(propagation = Propagation.MANDATORY)
    public void deductAll(List<OrderItem> items) {
        if (items == null) {
            return;
        }
        for (OrderItem item : items) {
            deduct(item.getProductId(), item.getQuantity());
        }
    }

    /** 恢复库存（取消/退款/关单），库存为 null 时按 0 处理 */
    @Transactional(propagation = Propagation.MANDATORY)
    public void restore(String productId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        productRepository.findByIdForUpdate(productId).ifPresent(product -> {
            int stock = product.getStock() != null ? product.getStock() : 0;
            product.setStock(stock + quantity);
            if ("sold_out".equals(product.getStatus())) {
                product.setStatus("active");
            }
            productRepository.save(product);
        });
    }

    /** 批量恢复（按订单明细） */
    @Transactional(propagation = Propagation.MANDATORY)
    public void restoreAll(List<OrderItem> items) {
        if (items == null) {
            return;
        }
        for (OrderItem item : items) {
            restore(item.getProductId(), item.getQuantity());
        }
    }

    /**
     * 按订单号恢复该订单已预占的全部库存（超时未支付关单场景）。
     *
     * <p>恢复失败只记录日志不中断：关单流程不应因为个别商品不存在而整体回滚，
     * 否则超时流水会永久悬挂、库存也无法归还。
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void restoreByOrderId(String orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) {
            return;
        }
        for (OrderItem item : items) {
            try {
                restore(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("[库存恢复] 失败，orderId={} productId={}", orderId, item.getProductId(), e);
            }
        }
    }
}
