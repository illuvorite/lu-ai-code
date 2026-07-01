package com.lu.luaicode.service.impl;


import com.lu.luaicode.mapper.ChatHistoryMapper;
import com.lu.luaicode.model.dto.entity.ChatHistory;
import com.lu.luaicode.service.ChatHistoryService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author illusory
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService {

}
