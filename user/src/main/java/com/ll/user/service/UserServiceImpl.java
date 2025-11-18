package com.ll.user.service;

import com.ll.user.exception.UserNotFoundException;
import com.ll.user.messaging.producer.UserEventProducer;
import com.ll.user.model.entity.User;
import com.ll.user.model.vo.request.UserLoginRequest;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.user.model.vo.response.UserLoginResponse;
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final UserEventProducer userEventProducer;

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return UserResponse.from(user);
    }

    @Override
    public UserResponse getUserByUserCode(String userCode) {
        User user = userRepository.findByCode(userCode).orElseThrow(UserNotFoundException::new);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UserPatchRequest request, String userCode) {
        User user = userRepository.findByCode(userCode)
                .orElseThrow(UserNotFoundException::new);
        modelMapper.map(request,user);
        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    @Override
    public List<UserResponse> getUserList() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserLoginResponse createOrUpdateUser(UserLoginRequest request) {
        Optional<User> existing = userRepository.findBySocialIdAndSocialProvider(request.socialId(), request.socialProvider());
        User savedUser;
        if(existing.isPresent()) {
            User user = existing.get();
            user.updateSocialInfo(
                    request.socialId(),
                    request.socialProvider(),
                    request.email(),
                    request.name()
            );
            savedUser = userRepository.save(user);
        }
        else {
            User user = User.builder()
                    .socialId(request.socialId())
                    .socialProvider(request.socialProvider())
                    .email(request.email())
                    .name(request.name())
                    .build();
            savedUser = userRepository.save(user);
            userEventProducer.sendDeposit(savedUser.getId(),savedUser.getCode());
            userEventProducer.sendCart(savedUser.getId(),savedUser.getCode());
        }
        return UserLoginResponse.from(savedUser);
    }
}
