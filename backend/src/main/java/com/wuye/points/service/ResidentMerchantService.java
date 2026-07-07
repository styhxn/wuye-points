package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.resident.ConsumedMerchantListItemResponse;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.Resident;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidentMerchantService {

    private final OrderRepository orderRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;

    @Transactional(readOnly = true)
    public PageResponse<ConsumedMerchantListItemResponse> listConsumedMerchants(int page, int pageSize,
            String keyword, String sort) {
        roleGuard.requireAnyRole("resident");
        String userId = UserContext.currentUserId();
        Resident resident = getActiveResident(userId);

        Sort springSort = parseSort(sort);
        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1,
                Math.min(Math.max(pageSize, 1), 100),
                springSort);

        Page<Order> orderPage = orderRepository.findByResidentId(resident.getId(), pageable);

        List<String> merchantIds = orderPage.getContent().stream()
                .map(Order::getMerchantId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        if (merchantIds.isEmpty()) {
            return PageResponse.of(List.of(), page, pageSize, 0);
        }

        Map<String, Merchant> merchantMap = merchantRepository.findAllById(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getId, m -> m));

        Map<String, Long> orderCountMap = orderPage.getContent().stream()
                .filter(o -> "completed".equals(o.getOrderStatus()))
                .collect(Collectors.groupingBy(Order::getMerchantId, Collectors.counting()));

        Map<String, Order> lastOrderMap = orderPage.getContent().stream()
                .filter(o -> StringUtils.hasText(o.getMerchantId()) && o.getPaidAt() != null)
                .collect(Collectors.toMap(
                        Order::getMerchantId,
                        o -> o,
                        (a, b) -> a.getPaidAt().isAfter(b.getPaidAt()) ? a : b));

        List<ConsumedMerchantListItemResponse> list = merchantIds.stream()
                .filter(merchantMap::containsKey)
                .map(merchantId -> {
                    Merchant m = merchantMap.get(merchantId);
                    Order lastOrder = lastOrderMap.get(merchantId);
                    Long count = orderCountMap.getOrDefault(merchantId, 0L);
                    return ConsumedMerchantListItemResponse.builder()
                            .merchantId(m.getId())
                            .merchantName(m.getName())
                            .category(m.getCategory())
                            .coverUrls(m.getCoverUrls())
                            .merchantLevel(m.getMerchantLevel())
                            .orderCount(count.intValue())
                            .lastOrderAt(lastOrder != null && lastOrder.getPaidAt() != null
                                    ? DateTimeUtil.toIsoUtc(lastOrder.getPaidAt())
                                    : null)
                            .build();
                })
                .filter(item -> {
                    if (StringUtils.hasText(keyword)) {
                        return item.getMerchantName() != null
                                && item.getMerchantName().toLowerCase().contains(keyword.toLowerCase());
                    }
                    return true;
                })
                .toList();

        int total = list.size();
        int from = (int) pageable.getOffset();
        int to = Math.min(from + pageable.getPageSize(), total);
        List<ConsumedMerchantListItemResponse> pagedList = from < total ? list.subList(from, to) : List.of();

        return PageResponse.of(pagedList, page, pageSize, total);
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        boolean descending = sort.startsWith("-");
        String field = sort.startsWith("-") || sort.startsWith("+") ? sort.substring(1) : sort;
        String springField = switch (field) {
            case "lastOrderAt" -> "paidAt";
            case "orderCount" -> "createdAt";
            default -> "createdAt";
        };
        return Sort.by(descending ? Sort.Direction.DESC : Sort.Direction.ASC, springField);
    }

    private Resident getActiveResident(String userId) {
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }
}
