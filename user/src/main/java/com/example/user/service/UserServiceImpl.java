package com.example.user.service;

import com.example.user.model.entity.User;
import com.example.user.model.vo.request.UserLoginRequest;
import com.example.user.model.vo.request.UserPatchRequest;
import com.example.user.model.vo.response.UserLoginResponse;
import com.example.user.model.vo.response.UserPatchResponse;
import com.example.user.model.vo.response.UserResponse;
import com.example.user.repository.UserRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(NoSuchElementException::new);
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    @Transactional
    public UserPatchResponse updateUser(UserPatchRequest request, Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException("유저아이디가 필요합니다");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다:" + userId));
        modelMapper.map(request,user);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserPatchResponse.class);
    }

    @Override
    public List<UserResponse> getUserList() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user,UserResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserLoginResponse createOrUpdateUser(UserLoginRequest request) {
        Optional<User> existing = userRepository.findBySocialIdAndSocialProvider(request.socialId(), request.socialProvider());

        String userCode = "USER-" + UuidCreator.getTimeOrderedEpoch().toString().substring(0, 8).toUpperCase();

        if(existing.isPresent()) {
            User user = existing.get();
            modelMapper.map(request,user);
            return modelMapper.map(user,UserLoginResponse.class);
        }
        else{

            User user = User.builder()
                    .socialId(request.socialId())
                    .socialProvider(request.socialProvider())
                    .userCode(userCode)
                    .email(request.email())
                    .name(request.name()).build();

            User savedUser = userRepository.save(user);
            return modelMapper.map(savedUser,UserLoginResponse.class);
        }

    }
}
