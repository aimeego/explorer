package io.nebulas.explorer.service.blockchain;

import io.nebulas.explorer.domain.NasAccount;
import io.nebulas.explorer.domain.NasAccountCondition;
import io.nebulas.explorer.mapper.NasAccountMapper;
import io.nebulas.explorer.mapper.NebAddressMapper;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.Date;

@Service
@AllArgsConstructor
public class NasAccountService {

    private final NasAccountMapper nasAccountMapper;
    private final NebAddressMapper nebAddressMapper;

    public NasAccount getLatestNasAccount() {
        NasAccount nasAccount = nasAccountMapper.selectByLatest();
        if (nasAccount == null) {
            nasAccount = nasAccountMapper.selectByLatest();
        }
        return nasAccount;
    }

    public NasAccount getNasAccountFromNinetyDays() {
        //找90天前的account记录
        NasAccountCondition cond = new NasAccountCondition();

        DateTime today = new DateTime(DateTimeZone.UTC).withTimeAtStartOfDay().toDateTime(DateTimeZone.getDefault());
        DateTime ninetyDay = today.minusDays(90);
        cond.createCriteria().andTimestampEqualTo(ninetyDay.withTimeAtStartOfDay().toDate());

        List<NasAccount> nasAccount = nasAccountMapper.selectByCondition(cond);
        if (nasAccount.size() == 0) {
            //如果没有数据再统计一遍
            NasAccount tmp = new NasAccount();
            tmp.setAddressCount((int)getAddressCountUntilDay(ninetyDay.withTimeAtStartOfDay().toDate()));
            return tmp;
        }
        return nasAccount.get(0);
    }

    //获取八周内的账户变化数据
    public List<NasAccount> getEightWeeks() {

        NasAccountCondition cond = new NasAccountCondition();
        DateTime today = new DateTime().withTimeAtStartOfDay().toDateTime(DateTimeZone.getDefault());
        DateTime endDay = today.minusDays(1);
        DateTime startDay = endDay.minusDays(56);
        cond.createCriteria().andTimestampBetween(startDay.toDate(), endDay.toDate());
        List<NasAccount> nasAccountList = nasAccountMapper.selectByCondition(cond);
        if (nasAccountList.size() == 0) {
            return Collections.emptyList();
        }

        //每隔8天把数据取出来
        List<NasAccount> result = new ArrayList<>();
        for (int i = 0; i < nasAccountList.size(); i++) {
            if (i % 7 == 0) {
                result.add(nasAccountList.get(i));
            }
        }
        return result;
    }

    public long getAddressCountUntilDay(Date day){
        long addrCnt = nebAddressMapper.getAddressCountUntilDay(day);
        NasAccount record = new NasAccount();
        record.setTimestamp(day);
        record.setCreatedAt(day);
        record.setUpdatedAt(day);
        record.setAddressCount((int)addrCnt);
	record.setAddressIncrement(0);
        record.setContractCount(0);
        record.setContractIncrement(0);
        nasAccountMapper.insert(record);
        return addrCnt;
    }
}
