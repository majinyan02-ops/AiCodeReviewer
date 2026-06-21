package com.aicode.service;

import com.aicode.dto.GitSyncMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitNotifyService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifySyncStart(Long projectId) {
        GitSyncMessage message = new GitSyncMessage(projectId, "syncing", "仓库同步中...");
        //Spring WebSocket 消息模板，用于向指定目的地发送消息
        messagingTemplate.convertAndSend("/topic/git-sync/" + projectId, message);
        log.info("WebSocket 通知: syncStart, projectId={}", projectId);
    }

    public void notifySyncSuccess(Long projectId, String message) {
        GitSyncMessage msg = new GitSyncMessage(projectId, "success", message);
        messagingTemplate.convertAndSend("/topic/git-sync/" + projectId, msg);
        log.info("WebSocket 通知: syncSuccess, projectId={}", projectId);
    }

    public void notifySyncError(Long projectId, String error) {
        GitSyncMessage msg = new GitSyncMessage(projectId, "error", error);
        messagingTemplate.convertAndSend("/topic/git-sync/" + projectId, msg);
        log.info("WebSocket 通知: syncError, projectId={}", projectId);
    }
}
