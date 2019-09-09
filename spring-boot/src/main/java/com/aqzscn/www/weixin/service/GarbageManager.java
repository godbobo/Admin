package com.aqzscn.www.weixin.service;

import com.aqzscn.www.global.util.LettuceUtil;
import com.aqzscn.www.weixin.domain.CustomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import weixin.popular.bean.message.EventMessage;

import java.util.Calendar;

/**
 * @author Godbobo
 * @version 1.0
 * @date 2019/9/5 20:54
 */
@Component
public class GarbageManager implements CustomFilter {

    private final Logger logger = LoggerFactory.getLogger(GarbageManager.class);

    private String res;

    @Value("${spring.redis.keyPrefix.all}${spring.redis.keyPrefix.wxfunc}")
    private String redisPrefix;

    private LettuceUtil lettuceUtil = new LettuceUtil();

    private final String key = "2";

    @Override
    public String getResult() {
        return this.res;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public boolean next(EventMessage eventMessage) {
        if (eventMessage.getContent().equals(this.key)) {
            String k = this.redisPrefix + eventMessage.getFromUserName();
            this.lettuceUtil.set(key, this.key);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 30);
            this.lettuceUtil.expireAt(k, calendar.getTime());
            this.res = "欢迎使用垃圾分类查询，回复以下数字使用对应功能：";
            return false;
        }
        this.logger.info("我是垃圾分类相关服务管理者");
        this.res = eventMessage.getContent() + ":garbage";
        return true;
    }
}
