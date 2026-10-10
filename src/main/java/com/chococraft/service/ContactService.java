package com.chococraft.service;

import com.chococraft.entity.ContactMessage;
import com.chococraft.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Transactional
    public ContactMessage saveMessage(ContactMessage message) {
        return contactMessageRepository.save(message);
    }

    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAllByOrderBySentAtDesc();
    }
}
