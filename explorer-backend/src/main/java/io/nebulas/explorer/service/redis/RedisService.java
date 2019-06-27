package io.nebulas.explorer.service.redis;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.nebulas.explorer.domain.NebTxCountByDay;
import io.nebulas.explorer.mapper.NebTxCountByDayMapper;
import io.nebulas.explorer.service.thirdpart.nebulas.bean.Block;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class RedisService {

    private final static String REDIS_KEY_PREFIX = "tx_today_";

    @Qualifier("customStringTemplate")
    private final StringRedisTemplate redisTemplate;

    public void plusCount(Block block) {
        DateTime today = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay();
        DateTime blockTime = new DateTime(block.getTimestamp()*1000, DateTimeZone.UTC).withTimeAtStartOfDay();
        String format = today.toString("yyyy-MM-dd");
        String blockTimeFormat = blockTime.toString("yyyy-MM-dd");
        if (!format.equals(blockTimeFormat)) {
            return;
        }

        String redisKey = REDIS_KEY_PREFIX + format;
        String cache = redisTemplate.opsForValue().get(redisKey);

        int txCountInBlock = block.getTransactions().size();
        if (cache == null) {
            redisTemplate.opsForValue().set(redisKey, txCountInBlock + "");
            DateTime yesterday = today.minusDays(1);
            String yesterdayFormat = yesterday.toString("yyyy-MM-dd");
            String keyYesterday = REDIS_KEY_PREFIX + yesterdayFormat;
            redisTemplate.delete(keyYesterday);
        } else {
            if (txCountInBlock > 0) {
                int cachedCount = Integer.parseInt(cache);
                int countNow = cachedCount + txCountInBlock;
                redisTemplate.opsForValue().set(redisKey, Integer.toString(countNow));
            }
        }
    }
}
