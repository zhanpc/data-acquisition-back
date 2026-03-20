package com.maplestone.dataCollect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TDengine 双模式连接配置。
 * 支持 native(JNI) 和 ws(WebSocket) 两种连接方式。
 */
@Data
@ConfigurationProperties(prefix = "tdengine.connection")
public class TDengineConnectionProperties {

    /**
     * 连接模式: native | ws
     */
    private String mode = "native";

    private String database = "iot_data";
    private String username = "root";
    private String password = "taosdata";
    private String charset = "UTF-8";
    private String locale = "en_US.UTF-8";
    private String timezone = "UTC-8";

    private Native nativeConfig = new Native();
    private Ws ws = new Ws();

    @Data
    public static class Native {
        private String driverClassName = "com.taosdata.jdbc.TSDBDriver";
        private String host = "127.0.0.1";
        private Integer port = 6030;
    }

    @Data
    public static class Ws {
        private String driverClassName = "com.taosdata.jdbc.rs.RestfulDriver";
        private String host = "127.0.0.1";
        private Integer port = 6041;
    }
}
