package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.entity.Member;
import cloud.hilang.cs_member.entity.MemberCard;
import cloud.hilang.cs_member.entity.PointsRecord;
import cloud.hilang.cs_member.mapper.MemberCardMapper;
import cloud.hilang.cs_member.mapper.MemberMapper;
import cloud.hilang.cs_member.mapper.PointsRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;

/**
 * 会员注册服务类
 * 处理会员注册时的复杂初始化逻辑
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberRegisterService {

    private final MemberMapper memberMapper;
    private final MemberCardMapper memberCardMapper;
    private final PointsRecordMapper pointsRecordMapper;

    /**
     * 会员注册初始化
     * 在事务中处理会员、会员卡、积分记录的创建
     *
     * @param member 会员信息
     * @param registerPoints 注册积分
     * @param registerStoreId 注册门店ID
     * @return 创建的会员ID
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Long registerMemberWithInit(Member member, Integer registerPoints, Long registerStoreId) {
        try {
            // 1. 插入会员记录
            member.setRegisterStoreId(registerStoreId);
            memberMapper.insert(member);
            Long memberId = member.getId();

            log.info("会员记录创建成功: memberId={}, memberNo={}", memberId, member.getMemberNo());

            // 2. 创建会员卡记录
            createMemberCard(memberId, member.getMemberNo());

            // 3. 如果有注册积分，创建积分记录
            if (registerPoints != null && registerPoints > 0) {
                createPointsRecord(memberId, registerPoints, registerStoreId);
                // 更新会员积分信息
                updateMemberPoints(memberId, registerPoints);
            }

            log.info("会员注册初始化完成: memberId={}, memberNo={}, registerPoints={}",
                    memberId, member.getMemberNo(), registerPoints);

            return memberId;

        } catch (Exception e) {
            log.error("会员注册初始化失败: phone={}, error={}", member.getPhone(), e.getMessage());
            throw new RuntimeException("会员注册失败: " + e.getMessage());
        }
    }

    /**
     * 创建会员卡记录
     */
    private void createMemberCard(Long memberId, String memberNo) {
        MemberCard memberCard = new MemberCard();
        memberCard.setMemberId(memberId);
        memberCard.setMemberNo(memberNo);
        memberCard.setCardStatus(1); // 正常状态
        memberCard.setIssueDate(LocalDateTime.now());
        // 设置一年后过期
        memberCard.setExpireDate(LocalDateTime.now().plusYears(1));
        memberCard.setCreatedTime(LocalDateTime.now());

        memberCardMapper.insertMemberCard(memberCard);
        log.debug("会员卡创建成功: memberId={}, memberNo={}", memberId, memberNo);
    }

    /**
     * 创建积分记录
     */
    private void createPointsRecord(Long memberId, Integer points, Long storeId) {
        PointsRecord pointsRecord = new PointsRecord();
        pointsRecord.setMemberId(memberId);
        pointsRecord.setChangeType("earn"); // 获得积分
        pointsRecord.setPointsChange(points);
        pointsRecord.setPointsBefore(0);
        pointsRecord.setPointsAfter(points);
        pointsRecord.setChangeReason("会员注册赠送积分");
        pointsRecord.setReferenceType("register"); // 注册类型
        pointsRecord.setReferenceId(memberId);
        pointsRecord.setStoreId(storeId);
        pointsRecord.setOperatorId(null);
        pointsRecord.setOperatorName("系统自动");
        // 设置积分过期时间（一年后）
        pointsRecord.setExpireTime(LocalDateTime.now().plusYears(1));
        pointsRecord.setRecordTime(LocalDateTime.now());

        pointsRecordMapper.insertPointsRecord(pointsRecord);
        log.debug("积分记录创建成功: memberId={}, points={}", memberId, points);
    }

    /**
     * 更新会员积分信息
     */
    private void updateMemberPoints(Long memberId, Integer points) {
        // 只更新会员表的积分信息
        memberMapper.updatePoints(memberId, points, points);
        log.debug("会员积分更新成功: memberId={}, points={}", memberId, points);
    }

    /**
     * 验证手机号唯一性（包括已删除的记录）
     */
    public boolean isPhoneUnique(String phone) {
        Member existingMember = memberMapper.selectPhoneExists(phone);
        return existingMember == null;
    }

    /**
     * 验证邮箱唯一性（包括已删除的记录）
     */
    public boolean isEmailUnique(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // 邮箱为空时不验证唯一性
        }
        Member existingMember = memberMapper.selectEmailExists(email);
        return existingMember == null;
    }
}