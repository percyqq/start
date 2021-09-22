package org.learn.message.mq.parse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * SkuType相关枚举
 */
@Getter
@AllArgsConstructor
public class OtQueue {

    @Getter
    @AllArgsConstructor
    public enum SkuType {
        /**
         * mind增加类型
         **/
        ADD(1, "新增类型"),
        /**
         * mind修改类型
         **/
        EDIT(2, "修改类型"),
        /**
         * mind删除类型
         **/
        DELETE(3, "删除类型"),
        /**
         * mind启用类型
         **/
        ENABLE(4, "启用类型"),
        /**
         * mind停用类型
         **/
        DISABLE(5, "停用类型");


        private int type;
        private String text;

        /**
         * 通过op获取SupportMsg
         */
        public static SupportMsg getSupportMsg(int op) {
            SkuType[] array = SkuType.values();
            for (SkuType skuType : array) {
                if (op == skuType.type) {
                    return new SupportMsg(true, skuType.text);
                }
            }
            return new SupportMsg(false, "未注册类型");
        }

        @Data
        @AllArgsConstructor
        public static class SupportMsg {
            private boolean support;
            private String text;
        }

        public boolean equalsType(int op) {
            return type == op;
        }
    }
}