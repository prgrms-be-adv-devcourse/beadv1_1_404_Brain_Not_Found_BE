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
        return UserResponse.from(findUserByIdOrThrow(id));
    }

    @Override
    public UserResponse getUserByUserCode(String userCode) {
        return UserResponse.from(findUserByCodeOrThrow(userCode));
    }

    @Override
    @Transactional
    public UserResponse updateUser(UserPatchRequest request, String userCode) {
        User user = findUserByCodeOrThrow(userCode);
        modelMapper.map(request,user);
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public List<UserResponse> getUserList() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public UserLoginResponse createOrUpdateUser(UserLoginRequest request) {
        User savedUser = userRepository
                .findBySocialIdAndSocialProvider(request.socialId(), request.socialProvider())
                .map(existing -> updateExistingUser(existing,request))
                .orElseGet(() -> createUser(request));

        return UserLoginResponse.from(savedUser);
    }

    private User findUserByIdOrThrow(Long id){
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    private User findUserByCodeOrThrow(String code){
        return userRepository.findByCode(code).orElseThrow(UserNotFoundException::new);
    }

    private User updateExistingUser(User existingUser,UserLoginRequest request){
        existingUser.updateSocialInfo(
                request.email(),
                request.name()
        );

        return userRepository.save(existingUser);
    }

    private User createUser(UserLoginRequest request){
        User savedUser = userRepository.save(User.builder()
                .socialId(request.socialId())
                .socialProvider(request.socialProvider())
                .email(request.email())
                .name(request.name())
                .build());

        userEventProducer.sendDeposit(savedUser.getId(),savedUser.getCode());
        userEventProducer.sendCart(savedUser.getId(),savedUser.getCode());
        return savedUser;
    }
}
