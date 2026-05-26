package cloud.hilang.cs_member.service.impl;

import cloud.hilang.cs_member.entity.Member;
import cloud.hilang.cs_member.entity.MemberGradeConfig;
import cloud.hilang.cs_member.entity.SysUser;
import cloud.hilang.cs_member.entity.Store;
import cloud.hilang.cs_member.mapper.*;
import cloud.hilang.cs_member.service.DashboardService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MemberMapper memberMapper;
    private final ConsumptionRecordMapper consumptionRecordMapper;
    private final StoreMapper storeMapper;
    private final MemberGradeConfigMapper memberGradeConfigMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取当前用户信息（从 SecurityContext 获取）
        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return stats;
        }

        // 用户角色信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("roleCode", currentUser.getRoleCode());
        userInfo.put("roleName", currentUser.getRoleName());
        userInfo.put("name", currentUser.getName());
        userInfo.put("realName", currentUser.getName());
        userInfo.put("storeId", currentUser.getStoreId());
        if (currentUser.getStoreId() != null) {
            Store store = storeMapper.selectById(currentUser.getStoreId());
            userInfo.put("storeName", store != null ? store.getStoreName() : null);
        }
        stats.put("userInfo", userInfo);

        String roleCode = currentUser.getRoleCode();
        // 基础统计（所有角色都有）
        stats.putAll(getBasicStats(currentUser, roleCode));

        // 财务统计（仅 ADMIN 和 STORE_ADMIN 有）
        if ("SYSTEM_ADMIN".equals(roleCode) || "STORE_ADMIN".equals(roleCode)) {
            stats.putAll(getFinancialStats(currentUser, roleCode));
        } else {
            // STAFF 角色的财务数据设置为 0
            stats.put("todayRevenue", BigDecimal.ZERO);
            stats.put("revenueGrowth", 0);
        }

        // 管理员专属统计（仅 SYSTEM_ADMIN 有）
        if ("SYSTEM_ADMIN".equals(roleCode)) {
            stats.putAll(getAdminStats());
        }

        return stats;
    }

    @Override
    public Map<String, Object> getRevenueTrend(Integer period) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> dailyRevenues = new ArrayList<>();

        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            result.put("dailyRevenues", dailyRevenues);
            return result;
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(period - 1);
        Long storeId = "SYSTEM_ADMIN".equals(currentUser.getRoleCode()) ? null : currentUser.getStoreId();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            Map<String, Object> dailyStats = consumptionRecordMapper.getDailyStats(startOfDay, endOfDay, storeId);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            if ("SYSTEM_ADMIN".equals(currentUser.getRoleCode()) || "STORE_ADMIN".equals(currentUser.getRoleCode())) {
                dayData.put("revenue", dailyStats.getOrDefault("revenue", BigDecimal.ZERO));
            } else {
                dayData.put("revenue", BigDecimal.ZERO);
            }
            dayData.put("orderCount", dailyStats.getOrDefault("orderCount", 0L));
            dailyRevenues.add(dayData);
        }
        result.put("dailyRevenues", dailyRevenues);
        return result;
    }

    @Override
    public List<Map<String, Object>> getMemberLevelDistribution() {
        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> distribution = new ArrayList<>();
        // 由于已移除会员等级功能，返回简单的总会员统计
        Long storeId = "SYSTEM_ADMIN".equals(currentUser.getRoleCode()) ? null : currentUser.getStoreId();
        Long totalMembers = memberMapper.countByStore(storeId);

        Map<String, Object> levelData = new HashMap<>();
        levelData.put("level", 1);
        levelData.put("levelName", "会员");
        levelData.put("memberCount", totalMembers);
        levelData.put("percentage", 100.0);
        distribution.add(levelData);
        return distribution;
    }

    @Override
    public Map<String, Object> getPaymentMethodStats() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Long> paymentMethods = new HashMap<>();
        Map<String, Double> percentages = new HashMap<>();

        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            result.put("paymentMethods", paymentMethods);
            result.put("percentages", percentages);
            result.put("totalOrders", 0L);
            return result;
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        Long storeId = "SYSTEM_ADMIN".equals(currentUser.getRoleCode()) ? null : currentUser.getStoreId();
        List<Map<String, Object>> paymentStats = consumptionRecordMapper.getPaymentMethodStats(startOfDay, endOfDay, storeId);
        Long totalOrders = 0L;
        for (Map<String, Object> stat : paymentStats) {
            String method = (String) stat.get("paymentMethod");
            Long count = ((Number) stat.get("count")).longValue();
            paymentMethods.put(method, count);
            totalOrders += count;
        }
        for (Map.Entry<String, Long> entry : paymentMethods.entrySet()) {
            Double pct = totalOrders > 0 ? (entry.getValue().doubleValue() / totalOrders.doubleValue()) * 100 : 0.0;
            percentages.put(entry.getKey(), Math.round(pct * 100.0) / 100.0);
        }
        result.put("paymentMethods", paymentMethods);
        result.put("percentages", percentages);
        result.put("totalOrders", totalOrders);
        return result;
    }

    @Override
    public Map<String, Object> getConsumptionTimeDistribution() {
        Map<String, Object> result = new HashMap<>();
        Map<String, BigDecimal> distribution = new HashMap<>();

        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            result.put("distribution", distribution);
            result.put("totalOrders", 0L);
            result.put("peakHour", null);
            result.put("peakHourCount", 0L);
            return result;
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        Long storeId = "SYSTEM_ADMIN".equals(currentUser.getRoleCode()) ? null : currentUser.getStoreId();
        List<Map<String, Object>> timeStats = consumptionRecordMapper.getConsumptionTimeStats(startOfDay, endOfDay, storeId);
        Long totalOrders = 0L;
        String peakHour = null;
        Long peakHourCount = 0L;
        for (Map<String, Object> stat : timeStats) {
            String hour = String.format("%02d:00", stat.get("hour"));
            BigDecimal amount = (BigDecimal) stat.get("amount");
            Long count = ((Number) stat.get("count")).longValue();
            distribution.put(hour, amount);
            totalOrders += count;
            if (count > peakHourCount) {
                peakHourCount = count;
                peakHour = hour;
            }
        }
        result.put("distribution", distribution);
        result.put("totalOrders", totalOrders);
        result.put("peakHour", peakHour);
        result.put("peakHourCount", peakHourCount);
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentActivities() {
        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }
        Long storeId = "SYSTEM_ADMIN".equals(currentUser.getRoleCode()) ? null : currentUser.getStoreId();
        Integer limit = "SYSTEM_ADMIN".equals(currentUser.getRoleCode()) ? 20 : 10;
        List<Map<String, Object>> activities = consumptionRecordMapper.getRecentActivities(storeId, limit);
        for (Map<String, Object> activity : activities) {
            LocalDateTime createTime = (LocalDateTime) activity.get("createTime");
            if (createTime != null) {
                activity.put("time", createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if ("STAFF".equals(currentUser.getRoleCode())) {
                activity.remove("amount");
            }
        }
        return activities;
    }

    @Override
    public Map<String, Object> getBusinessStatistics(LocalDate startDate, LocalDate endDate, Long storeId) {
        Map<String, Object> result = new HashMap<>();
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        SysUser currentUser = getCurrentUser();
        if (currentUser != null && !"SYSTEM_ADMIN".equals(currentUser.getRoleCode())) {
            storeId = currentUser.getStoreId();
        }
        Map<String, Object> stats = consumptionRecordMapper.getBusinessStatistics(startDateTime, endDateTime, storeId);
        result.putAll(stats);
        return result;
    }

    @Override
    public Map<String, Object> getMemberGrowth(LocalDate startDate, LocalDate endDate, Long storeId) {
        Map<String, Object> result = new HashMap<>();
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        SysUser currentUser = getCurrentUser();
        if (currentUser != null && !"SYSTEM_ADMIN".equals(currentUser.getRoleCode())) {
            storeId = currentUser.getStoreId();
        }
        // 将LocalDate转换为LocalDateTime
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        List<Map<String, Object>> growthDataList = memberMapper.getMemberGrowthData(startDateTime, endDateTime, storeId);
        result.put("growthData", growthDataList);
        if (!growthDataList.isEmpty()) {
            Map<String, Object> latest = growthDataList.get(growthDataList.size() - 1);
            result.put("totalMembers", latest.get("totalMembers"));
            result.put("newMembers", latest.get("newMembers"));
            result.put("activeMembers", latest.get("activeMembers"));
        }
        return result;
    }

    @Override
    public Map<String, Object> getRealTimeStats() {
        Map<String, Object> result = new HashMap<>();
        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return result;
        }
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        Long storeId = "SYSTEM_ADMIN".equals(currentUser.getRoleCode()) ? null : currentUser.getStoreId();
        Long todayNewMembers = memberMapper.countByDateRange(startOfDay, now, storeId);
        result.put("todayNewMembers", todayNewMembers);
        Long todayOrders = consumptionRecordMapper.countByDateRange(startOfDay, now, storeId);
        result.put("todayOrders", todayOrders);
        if ("SYSTEM_ADMIN".equals(currentUser.getRoleCode()) || "STORE_ADMIN".equals(currentUser.getRoleCode())) {
            BigDecimal todayRevenue = consumptionRecordMapper.getRevenueByDateRange(startOfDay, now, storeId);
            result.put("todayRevenue", todayRevenue);
        } else {
            result.put("todayRevenue", BigDecimal.ZERO);
        }
        result.put("onlineMembers", consumptionRecordMapper.getActiveMemberCount(startOfDay, now, storeId));
        result.put("updateTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return result;
    }

    /**
     * 获取当前登录用户（模拟实现）
     */
    /**
     * 获取当前登录用户
     */
    private SysUser getCurrentUser() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof cloud.hilang.cs_member.security.CustomUserDetails) {
                cloud.hilang.cs_member.security.CustomUserDetails userDetails = 
                    (cloud.hilang.cs_member.security.CustomUserDetails) authentication.getPrincipal();
                Long userId = userDetails.getUserId();
                return sysUserMapper.selectWithRolesById(userId);
            }
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
        }
        return null;
    }

    /**
     * 基础统计（所有角色都有）
     */
    private Map<String, Object> getBasicStats(SysUser currentUser, String roleCode) {
        Map<String, Object> stats = new HashMap<>();
        Long storeId = "SYSTEM_ADMIN".equals(roleCode) ? null : currentUser.getStoreId();
        Long totalMembers = memberMapper.countByStore(storeId);
        stats.put("totalMembers", totalMembers);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        Long todayNewMembers = memberMapper.countByDateRange(startOfDay, now, storeId);
        stats.put("todayNewMembers", todayNewMembers);
        stats.put("newMembers", todayNewMembers);
        Long monthNewMembers = memberMapper.countByDateRange(LocalDate.now().withDayOfMonth(1).atStartOfDay(), now, storeId);
        stats.put("monthNewMembers", monthNewMembers);
        Long totalOrders = consumptionRecordMapper.countByStore(storeId);
        stats.put("totalOrders", totalOrders);
        Long todayOrders = consumptionRecordMapper.countByDateRange(startOfDay, now, storeId);
        stats.put("todayOrders", todayOrders);
        Long monthOrders = consumptionRecordMapper.countByDateRange(LocalDate.now().withDayOfMonth(1).atStartOfDay(), now, storeId);
        stats.put("monthOrders", monthOrders);
        Long activeMembers = consumptionRecordMapper.getActiveMemberCount(now.minusDays(30), now, storeId);
        stats.put("activeMembers", activeMembers);
        return stats;
    }

    /**
     * 财务统计（ADMIN 和 STORE_ADMIN 有）
     */
    private Map<String, Object> getFinancialStats(SysUser currentUser, String roleCode) {
        Map<String, Object> stats = new HashMap<>();
        Long storeId = "SYSTEM_ADMIN".equals(roleCode) ? null : currentUser.getStoreId();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime yesterdayStart = now.minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime yesterdayEnd = now.minusDays(1).toLocalDate().atTime(LocalTime.MAX);
        BigDecimal todayRevenue = consumptionRecordMapper.getRevenueByDateRange(startOfDay, now, storeId);
        stats.put("todayRevenue", todayRevenue);
        BigDecimal yesterdayRevenue = consumptionRecordMapper.getRevenueByDateRange(yesterdayStart, yesterdayEnd, storeId);
        if (yesterdayRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growthRate = todayRevenue.subtract(yesterdayRevenue)
                    .divide(yesterdayRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            stats.put("revenueGrowth", growthRate.doubleValue());
        } else {
            stats.put("revenueGrowth", todayRevenue.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0);
        }
        BigDecimal monthRevenue = consumptionRecordMapper.getRevenueByDateRange(startOfMonth, now, storeId);
        stats.put("monthRevenue", monthRevenue);
        BigDecimal totalRevenue = consumptionRecordMapper.getRevenueByStore(storeId);
        stats.put("totalRevenue", totalRevenue);
        return stats;
    }

    /**
     * 管理员专属统计（仅 SYSTEM_ADMIN 有）
     */
    private Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        Long totalStores = storeMapper.selectCount(null);
        stats.put("totalStores", totalStores);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        Long todayNewStores = storeMapper.countByDateRange(startOfDay, now);
        stats.put("todayNewStores", todayNewStores);
        return stats;
    }
}