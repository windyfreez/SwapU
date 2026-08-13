package com.itsean.campus_second_hand.task;

import cn.hutool.json.JSONUtil;
import com.itsean.campus_second_hand.mapper.UserBehaviorLogMapper;
import com.itsean.pojo.entity.UserBehaviorLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.itsean.common.constant.StringConstant.BEHAVIOR_QUEUE_KEY;

@Slf4j
@Component
public class BehaviorTask {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserBehaviorLogMapper userBehaviorLogMapper;

    private static final int BATCH_SIZE = 500;

    @Scheduled(cron = "0 * * * * *")
    public void syncBehaviorData() {
        List<UserBehaviorLog> batchList = new ArrayList<>(BATCH_SIZE);

        for (int i = 0; i < BATCH_SIZE; i++) {
            String logJson = stringRedisTemplate.opsForList().leftPop(BEHAVIOR_QUEUE_KEY);
            if(logJson == null){
                break;
            }
            UserBehaviorLog logEntry = JSONUtil.toBean(logJson, UserBehaviorLog.class);
            batchList.add(logEntry);
        }

        if(!batchList.isEmpty()) {
            try {
                userBehaviorLogMapper.addBehavior(batchList);
                log.info("成功批量同步 {} 条用户行为日志到数据库", batchList.size());
            } catch (Exception e) {
                log.error("批量同步用户行为日志异常", e);
            }
        }

    }
}
