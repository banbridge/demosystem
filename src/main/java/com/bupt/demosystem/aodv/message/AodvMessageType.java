package com.bupt.demosystem.aodv.message;

/**
 * @Author banbridge
 * @Classname AodvMessageType
 * @Date 2021/6/3 09:37
 */
public enum AodvMessageType {

    /**
     * Route Request (RREQ) Message
     */
    RREQ,

    /**
     * Route Reply (RREP) Message
     */
    RREP,

    /**
     * Route Error (RERR) Message
     */
    RERR,

    /**
     * Route Reply Acknowledgment (RREP-ACK) Message
     */
    RREP_ACK,

    /**
     * 发送具体的消息
     */
    CONTENT

}
