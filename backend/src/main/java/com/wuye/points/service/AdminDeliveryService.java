package com.wuye.points.service;

import com.wuye.points.dto.delivery.AdminDeliveryOverviewResponse;
import com.wuye.points.dto.delivery.AdminDeliveryOverviewResponse.CourierItem;
import com.wuye.points.dto.delivery.AdminDeliveryOverviewResponse.RecentDelivery;
import com.wuye.points.dto.delivery.AdminDeliveryOverviewResponse.TodayStats;
import com.wuye.points.dto.delivery.DeliveryCapacityResponse;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDeliveryService {

    private static final Set<String> COURIER_ROLES = Set.of("courier");

    private final DeliveryRepository deliveryRepository;
    private final ResidentRepository residentRepository;
    private final OrderRepository orderRepository;
    private final AdminSupport adminSupport;
    private final RoleGuard roleGuard;

    @Transactional(readOnly = true)
    public AdminDeliveryOverviewResponse getOverview(String requestedPropertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        String propertyCompanyId = adminSupport.resolvePropertyCompanyId(requestedPropertyCompanyId);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();

        List<Resident> couriers = residentRepository.findByPropertyCompanyIdAndRole(propertyCompanyId, "courier");

        long todayOrders = countTodayDeliveries(propertyCompanyId, todayStart, todayEnd);
        long yesterdayOrders = countYesterdayDeliveries(propertyCompanyId);
        long orderGrowth = todayOrders - yesterdayOrders;

        long onlineCouriers = countOnlineCouriers(couriers, todayStart, todayEnd);
        long totalCouriers = couriers.size();

        BigDecimal todayDeliveryFee = sumTodayDeliveryFee(propertyCompanyId, todayStart, todayEnd);
        BigDecimal capacityLoad = calculateCapacityLoad(onlineCouriers, totalCouriers);

        List<CourierItem> courierItems = buildCourierItems(couriers, todayStart, todayEnd);
        List<RecentDelivery> recentDeliveries = getRecentDeliveries(propertyCompanyId);

        TodayStats todayStats = TodayStats.builder()
                .todayOrders(todayOrders)
                .orderGrowth(orderGrowth)
                .onlineCouriers(onlineCouriers)
                .totalCouriers(totalCouriers)
                .todayDeliveryFee(todayDeliveryFee)
                .capacityLoad(capacityLoad)
                .build();

        return AdminDeliveryOverviewResponse.builder()
                .todayStats(todayStats)
                .couriers(courierItems)
                .recentDeliveries(recentDeliveries)
                .build();
    }

    private long countTodayDeliveries(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        return deliveryRepository.countByPropertyCompanyIdAndCreatedAtBetween(propertyCompanyId, start, end);
    }

    private long countYesterdayDeliveries(String propertyCompanyId) {
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().atStartOfDay();
        return deliveryRepository.countByPropertyCompanyIdAndCreatedAtBetween(propertyCompanyId, yesterdayStart, yesterdayEnd);
    }

    private long countOnlineCouriers(List<Resident> couriers, LocalDateTime todayStart, LocalDateTime todayEnd) {
        List<String> courierIds = couriers.stream()
                .map(Resident::getId)
                .toList();
        if (courierIds.isEmpty()) return 0;

        List<Delivery> todayDeliveries = deliveryRepository.findByCourierIdInAndStatus(courierIds, "accepted");
        Set<String> activeCourierIds = todayDeliveries.stream()
                .map(Delivery::getCourierId)
                .collect(Collectors.toSet());
        return activeCourierIds.size();
    }

    private BigDecimal sumTodayDeliveryFee(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        BigDecimal sum = deliveryRepository.sumFeeByPropertyCompanyIdAndCreatedAtBetween(propertyCompanyId, start, end);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private BigDecimal calculateCapacityLoad(long online, long total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(online)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private List<CourierItem> buildCourierItems(List<Resident> couriers, LocalDateTime todayStart, LocalDateTime todayEnd) {
        List<CourierItem> items = new ArrayList<>();
        for (Resident courier : couriers) {
            long todayCompleted = deliveryRepository.countByCourierIdAndStatusAndDeliveredAtBetween(
                    courier.getId(), "delivered", todayStart, todayEnd);

            BigDecimal monthIncome = deliveryRepository.sumCourierEarningByCourierIdAndDeliveredAtBetween(
                    courier.getId(),
                    LocalDate.now().withDayOfMonth(1).atStartOfDay(),
                    todayEnd);
            if (monthIncome == null) monthIncome = BigDecimal.ZERO;

            boolean isOnline = deliveryRepository.existsByCourierIdAndStatusIn(
                    courier.getId(), List.of("accepted", "picked"));

            items.add(CourierItem.builder()
                    .id(courier.getId())
                    .name(courier.getName())
                    .todayCompleted(todayCompleted)
                    .monthIncome(monthIncome)
                    .status(isOnline ? "online" : "offline")
                    .build());
        }
        return items;
    }

    private List<RecentDelivery> getRecentDeliveries(String propertyCompanyId) {
        List<Delivery> deliveries = deliveryRepository.findTop10ByPropertyCompanyIdOrderByCreatedAtDesc(propertyCompanyId);
        if (deliveries.isEmpty()) return List.of();

        Set<String> orderIds = deliveries.stream()
                .map(Delivery::getOrderId)
                .collect(Collectors.toSet());
        Map<String, Order> orderMap = orderRepository.findAllById(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, o -> o));

        Set<String> courierIds = deliveries.stream()
                .map(Delivery::getCourierId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<String, Resident> courierMap = residentRepository.findAllById(courierIds).stream()
                .collect(Collectors.toMap(Resident::getId, r -> r));

        return deliveries.stream()
                .map(d -> {
                    Order order = orderMap.get(d.getOrderId());
                    Resident courier = d.getCourierId() != null ? courierMap.get(d.getCourierId()) : null;
                    return RecentDelivery.builder()
                            .id(d.getId())
                            .time(DateTimeUtil.toLocalDateTimeString(d.getCreatedAt()))
                            .residentName(courier != null ? courier.getName() : "待分配")
                            .productDesc(order != null ? "订单#" + order.getOrderNo() : "-")
                            .fee(d.getFee())
                            .status(d.getStatus())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public DeliveryCapacityResponse getCapacityAnalysis(String requestedPropertyCompanyId, String dimension) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        String propertyCompanyId = adminSupport.resolvePropertyCompanyId(requestedPropertyCompanyId);

        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();
        if ("7d".equals(dimension)) {
            start = end.minusDays(7).withHour(0).withMinute(0).withSecond(0);
        } else {
            start = end.toLocalDate().atStartOfDay();
        }

        List<Delivery> deliveries = deliveryRepository.findByPropertyCompanyIdAndCreatedAtBetween(
                propertyCompanyId, start, end);

        List<DeliveryCapacityResponse.HourlyData> hourlyData = calculateHourlyStats(deliveries, start, end);

        String peakHour = findPeakHour(hourlyData);

        return DeliveryCapacityResponse.builder()
                .dimension(dimension)
                .peakHour(peakHour)
                .hourlyData(hourlyData)
                .build();
    }

    private List<DeliveryCapacityResponse.HourlyData> calculateHourlyStats(
            List<Delivery> deliveries, LocalDateTime start, LocalDateTime end) {

        Map<Integer, List<Delivery>> byHour = deliveries.stream()
                .filter(d -> d.getCreatedAt() != null)
                .collect(Collectors.groupingBy(d -> d.getCreatedAt().getHour()));

        List<DeliveryCapacityResponse.HourlyData> result = new ArrayList<>();
        int startHour = start.getHour();
        int endHour = end.getHour();

        for (int h = startHour; h <= endHour; h++) {
            List<Delivery> hourDeliveries = byHour.getOrDefault(h, List.of());

            long count = hourDeliveries.size();
            BigDecimal avgResponse = calculateAvgResponseMinutes(hourDeliveries);

            result.add(DeliveryCapacityResponse.HourlyData.builder()
                    .hour(h)
                    .label(String.format("%02d:00", h))
                    .deliveryCount(count)
                    .avgResponseMinutes(avgResponse)
                    .build());
        }

        return result;
    }

    private BigDecimal calculateAvgResponseMinutes(List<Delivery> deliveries) {
        if (deliveries.isEmpty()) return BigDecimal.ZERO;

        long totalMinutes = 0;
        int counted = 0;
        for (Delivery d : deliveries) {
            if (d.getCreatedAt() != null && d.getAcceptedAt() != null) {
                long minutes = java.time.Duration.between(d.getCreatedAt(), d.getAcceptedAt()).toMinutes();
                totalMinutes += minutes;
                counted++;
            }
        }

        if (counted == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(counted), 1, RoundingMode.HALF_UP);
    }

    private String findPeakHour(List<DeliveryCapacityResponse.HourlyData> hourlyData) {
        return hourlyData.stream()
                .max((a, b) -> Long.compare(a.getDeliveryCount(), b.getDeliveryCount()))
                .filter(h -> h.getDeliveryCount() > 0)
                .map(DeliveryCapacityResponse.HourlyData::getLabel)
                .orElse("-");
    }
}
