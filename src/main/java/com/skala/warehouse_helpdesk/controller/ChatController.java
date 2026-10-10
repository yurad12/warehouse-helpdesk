package com.skala.warehouse_helpdesk.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.skala.warehouse_helpdesk.dto.AskRequest;
import com.skala.warehouse_helpdesk.dto.ChatResponse;
import com.skala.warehouse_helpdesk.service.HelpDeskService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final HelpDeskService helpDeskService;

    public ChatController(HelpDeskService helpDeskService) {
        this.helpDeskService = helpDeskService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody AskRequest request) {
        return helpDeskService.chat(request);
    }

    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody AskRequest request) {
        return helpDeskService.stream(request);
    }
}
