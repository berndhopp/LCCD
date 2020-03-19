package org.ftc.server.engine;

import org.ftc.server.db.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UUIDVerifier {

    @Autowired
    private UserRepository userRepository;

    @Cacheable("uuid-verify")
    public boolean verify(UUID uuid){
        return userRepository.existsById(uuid);
    }
}
