package cloud.hilang.cs_member.util;

import cloud.hilang.cs_member.mapper.MemberMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 会员号生成工具类
 * 生成格式为：M + 年月日(6位) + 4位序列号 = 11位数的会员号
 * 例如：M2511240001 = 2025年11月24日 + 序列号0001
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
public class MemberNoGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static final int MAX_SEQUENCE = 9999; // 改为4位序列号，降低重复概率
    private static String lastDate = "";

    // 依赖注入（需要在Spring环境中使用）
    private static MemberMapper memberMapper;

    public static void setMemberMapper(MemberMapper mapper) {
        memberMapper = mapper;
    }

    /**
     * 生成11位数的会员号
     * 格式：M + 年月日(YYMMDD) + 4位序列号
     *
     * @return 11位数的会员号字符串
     */
    public static synchronized String generateMemberNo() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DATE_FORMATTER); // 6位日期

        // 如果日期变化，重置序列号
        if (!datePart.equals(lastDate)) {
            SEQUENCE.set(0);
            lastDate = datePart;
        }

        // 生成4位序列号
        long sequence = SEQUENCE.incrementAndGet();
        if (sequence > MAX_SEQUENCE) {
            // 如果超过最大序列号，重置为1
            SEQUENCE.set(1);
            sequence = 1;
        }

        String sequencePart = String.format("%04d", sequence);
        String memberNo = "M" + datePart + sequencePart;

        // 如果有数据库访问能力，检查唯一性
        if (memberMapper != null) {
            // 确保生成的会员号在数据库中不存在
            int attempts = 0;
            while (memberMapper.selectByMemberNo(memberNo) != null && attempts < 100) {
                // 如果存在冲突，递增序列号重试
                sequence = SEQUENCE.incrementAndGet();
                if (sequence > MAX_SEQUENCE) {
                    SEQUENCE.set(1);
                    sequence = 1;
                }
                sequencePart = String.format("%04d", sequence);
                memberNo = "M" + datePart + sequencePart;
                attempts++;
            }

            if (attempts >= 100) {
                throw new RuntimeException("无法生成唯一的会员号，请稍后重试");
            }
        }

        return memberNo;
    }

    /**
     * 生成指定日期的会员号
     *
     * @param dateTime 指定日期时间
     * @return 11位数的会员号字符串
     */
    public static String generateMemberNo(LocalDateTime dateTime) {
        String datePart = dateTime.format(DATE_FORMATTER); // 6位日期

        // 生成4位随机序列号（用于历史数据）
        long sequence = ThreadLocalRandom.current().nextLong(1, MAX_SEQUENCE + 1);
        String sequencePart = String.format("%04d", sequence);

        return "M" + datePart + sequencePart;
    }

    /**
     * 验证会员号格式是否正确
     * 格式：M + YYMMDD + 4位数字
     *
     * @param memberNo 会员号
     * @return 是否为有效格式
     */
    public static boolean isValidMemberNo(String memberNo) {
        if (memberNo == null || memberNo.length() != 11 || !memberNo.startsWith("M")) {
            return false;
        }

        String numericPart = memberNo.substring(1);
        if (!numericPart.matches("\\d+")) {
            return false;
        }

        return true;
    }

    /**
     * 从会员号中提取日期部分
     *
     * @param memberNo 会员号
     * @return 日期字符串(YYMMDD格式)，如果格式无效返回null
     */
    public static String extractDateFromMemberNo(String memberNo) {
        if (!isValidMemberNo(memberNo)) {
            return null;
        }

        return memberNo.substring(1, 7); // 提取YYMMDD部分
    }

    /**
     * 重置序列号计数器（一般用于测试）
     */
    public static void resetSequence() {
        SEQUENCE.set(0);
        lastDate = "";
    }
}