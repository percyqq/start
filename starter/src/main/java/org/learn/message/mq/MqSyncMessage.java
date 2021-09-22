package org.learn.message.mq;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: 消息结构化对象
 */
@Slf4j
@Getter
@Setter
public class MqSyncMessage {
    private String topic;
    private String originMessage;
    private String msgId;
    private Integer operation;
    private Date serverUpdateTime;
    private Date currentTime;
    private Long brandIdenty;
    private Long shopIdenty;



    private List<Long> dishBrandIds = new ArrayList<>();

    public MqSyncMessage() {
        if (originMessage == null) {
            this.msgId = "mq-sync-default";
        }
    }

    public MqSyncMessage(String originMessage) {
        this.originMessage = originMessage;
        this.msgId = getMD5(originMessage);
    }

    public void setOriginMessage(String originMessage) {
        this.originMessage = originMessage;
        this.msgId = getMD5(originMessage);
    }

    public static String getMD5(String src) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md.digest(src.getBytes(Charset.forName("UTF-8")));
            String ret = Hex.encodeHexString(md5Bytes);
            return ret;
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
