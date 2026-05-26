package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.entity.Member;
import cloud.hilang.cs_member.entity.PointsRecord;
import cloud.hilang.cs_member.mapper.MemberMapper;
import cloud.hilang.cs_member.mapper.PointsRecordMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPointsService {

    private final MemberMapper memberMapper;
    private final PointsRecordMapper pointsRecordMapper;

    public Map<String, Object> getPointsBalance(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        Map<String, Object> result = new HashMap<>();
        if (member != null) {
            result.put("totalPoints", member.getTotalPoints());
            result.put("availablePoints", member.getCurrentPoints());
            result.put("totalEarned", member.getTotalPoints());
        }
        return result;
    }

    public PageInfo<PointsRecord> getPointsRecords(Long memberId, int pageNum, int pageSize,
                                                     Integer changeType, LocalDate startDate, LocalDate endDate) {
        PageHelper.startPage(pageNum, pageSize);
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
        List<PointsRecord> records = pointsRecordMapper.selectByMemberId(memberId);
        return new PageInfo<>(records);
    }

    public boolean exchangePoints(Long memberId, Long itemId, Integer points) {
        Member member = memberMapper.selectById(memberId);
        if (member == null || member.getCurrentPoints() < points) {
            return false;
        }
        int newPoints = member.getCurrentPoints() - points;
        member.setCurrentPoints(newPoints);
        member.setUpdatedTime(LocalDateTime.now());
        memberMapper.updateMemberInfo(member);

        PointsRecord record = new PointsRecord();
        record.setMemberId(memberId);
        record.setChangeType("use");
        record.setPointsChange(-points);
        record.setPointsBefore(member.getCurrentPoints() + points);
        record.setPointsAfter(newPoints);
        record.setChangeReason("积分兑换");
        record.setReferenceType("exchange");
        record.setReferenceId(itemId);
        record.setRecordTime(LocalDateTime.now());
        pointsRecordMapper.insertPointsRecord(record);
        return true;
    }

    public Map<String, Object> getPointsStatistics(Long memberId) {
        List<PointsRecord> records = pointsRecordMapper.selectByMemberId(memberId);
        int totalEarned = 0, totalUsed = 0;
        for (PointsRecord r : records) {
            if ("earn".equals(r.getChangeType())) totalEarned += r.getPointsChange();
            else if ("use".equals(r.getChangeType())) totalUsed += Math.abs(r.getPointsChange());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalEarned", totalEarned);
        result.put("totalUsed", totalUsed);
        return result;
    }
}
