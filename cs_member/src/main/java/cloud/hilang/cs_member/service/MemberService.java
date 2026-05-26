package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.dto.PageParams;
import cloud.hilang.cs_member.entity.Member;
import cloud.hilang.cs_member.mapper.MemberMapper;
import cloud.hilang.cs_member.util.MemberNoGenerator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.annotation.PostConstruct;

/**
 * 会员管理服务类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    // 初始化会员号生成器的Mapper依赖
    @PostConstruct
    public void init() {
        MemberNoGenerator.setMemberMapper(memberMapper);
    }

    /**
     * 分页查询会员列表
     */
    public PageInfo<Member> getMemberList(PageParams pageParams,
                                          String name, String phone, String memberNo,
                                          Integer memberLevel, Integer status,
                                          String storeName, Integer gender,
                                          LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("开始分页查询会员列表，页码：{}，页大小：{}", pageParams.getPageNum(), pageParams.getPageSize());

        String sortField = pageParams.getSortField();
        String sortOrder = pageParams.getSortOrder();

        // 处理排序字段和排序方向
        if (sortField == null || sortField.trim().isEmpty()) {
            sortField = "created_time"; // 默认排序字段
        }
        if (sortOrder == null || sortOrder.trim().isEmpty()) {
            sortOrder = "DESC"; // 默认排序方向
        }

        PageHelper.startPage(pageParams.getPageNum(), pageParams.getPageSize(),
                sortField + " " + sortOrder);

        // 查询会员列表
        List<Member> memberList = memberMapper.selectMemberList(name, phone, memberNo, status, storeName, gender, startTime, endTime);

        return new PageInfo<>(memberList);
    }

    /**
     * 根据ID查询会员详情
     */
    public Member getMemberById(Long id) {
        return memberMapper.selectById(id);
    }

    /**
     * 创建会员
     */
    @Transactional(rollbackFor = Exception.class)
    public Member createMember(Member member) {
        // 检查手机号是否已存在
        if (memberMapper.selectByPhone(member.getPhone()) != null) {
            throw new RuntimeException("手机号已存在");
        }

        // 生成会员编号
        member.setMemberNo(MemberNoGenerator.generateMemberNo());
        
        // 加密密码
        if (member.getPassword() != null) {
            member.setPassword(passwordEncoder.encode(member.getPassword()));
        } else {
            // 默认密码 123456
            member.setPassword(passwordEncoder.encode("123456"));
        }

        member.setCreatedTime(LocalDateTime.now());
        member.setUpdatedTime(LocalDateTime.now());
        member.setStatus(1); // 默认启用
        
        // 初始化其他字段
        if (member.getTotalPoints() == null) member.setTotalPoints(0);
        if (member.getCurrentPoints() == null) member.setCurrentPoints(0);
        if (member.getBalance() == null) member.setBalance(java.math.BigDecimal.ZERO);
        if (member.getTotalConsumption() == null) member.setTotalConsumption(java.math.BigDecimal.ZERO);

        memberMapper.insert(member);
        return member;
    }

    /**
     * 更新会员信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Member updateMember(Member member) {
        Member existingMember = memberMapper.selectById(member.getId());
        if (existingMember == null) {
            throw new RuntimeException("会员不存在");
        }

        // 如果修改了手机号，检查唯一性
        if (member.getPhone() != null && !member.getPhone().equals(existingMember.getPhone())) {
            if (memberMapper.selectByPhone(member.getPhone()) != null) {
                throw new RuntimeException("手机号已存在");
            }
        }

        member.setUpdatedTime(LocalDateTime.now());
        // 不允许通过此接口修改密码和余额积分等敏感信息
        member.setPassword(null);
        member.setBalance(null);
        member.setCurrentPoints(null);
        member.setTotalPoints(null);

        memberMapper.updateMemberInfo(member);
        return memberMapper.selectById(member.getId());
    }

    /**
     * 删除会员（逻辑删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long id) {
        Member member = memberMapper.selectById(id);
        if (member != null) {
            member.setStatus(2); // 2表示已删除/注销
            member.setUpdatedTime(LocalDateTime.now());
            memberMapper.updateById(member);
        }
    }

    /**
     * 批量删除会员
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteMembers(Long[] ids) {
        for (Long id : ids) {
            deleteMember(id);
        }
    }

    /**
     * 更新会员状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberStatus(Long id, Integer status) {
        Member member = new Member();
        member.setId(id);
        member.setStatus(status);
        member.setUpdatedTime(LocalDateTime.now());
        memberMapper.updateById(member);
    }

    /**
     * 重置会员密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetMemberPassword(Long id, String newPassword) {
        Member member = new Member();
        member.setId(id);
        member.setPassword(passwordEncoder.encode(newPassword));
        member.setUpdatedTime(LocalDateTime.now());
        memberMapper.updateById(member);
    }
}