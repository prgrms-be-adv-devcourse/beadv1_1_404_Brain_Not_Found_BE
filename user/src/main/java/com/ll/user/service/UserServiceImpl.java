package com.ll.user.service;

import com.ll.user.exception.UserNotFoundException;
import com.ll.user.model.entity.User;
import com.ll.user.model.vo.request.UserLoginRequest;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.user.model.vo.response.UserLoginResponse;
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

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
        User user;
        if(existing.isPresent()) {
            user = existing.get();
            user.updateSocialInfo(
                    request.socialId(),
                    request.socialProvider(),
                    request.email(),
                    request.name()
            );
        }
        else{

            user = User.builder()
                    .socialId(request.socialId())
                    .socialProvider(request.socialProvider())
                    .email(request.email())
                    .name(request.name())
                    .build();

        }

        User savedUser = userRepository.save(user);
        return UserLoginResponse.from(savedUser);
    }
}
