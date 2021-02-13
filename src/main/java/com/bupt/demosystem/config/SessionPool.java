package com.bupt.demosystem.config;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Banbridge on 2021/2/7.
 */
public class SessionPool {

    public static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<String, Session>();

    public static void addSession(String userID, Session session) {
        sessionMap.put(userID, session);
    }

    public static void removeSession(String userID) {
        sessionMap.remove(userID);
    }

}
