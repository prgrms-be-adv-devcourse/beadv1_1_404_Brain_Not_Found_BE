package com.ll.user;

import com.ll.user.model.entity.User;
import com.ll.user.model.enums.Grade;
import com.ll.user.model.enums.SocialProvider;
import com.ll.user.model.vo.request.UserLoginRequest;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.user.repository.UserRepository;
import com.ll.user.service.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

//@ExtendWith(MockitoExtension.class)
//@DisplayName("UserServiceImpl 테스트")
//class UserServiceImplTests {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private UserServiceImpl userService;
//
//    @Test
//    @DisplayName("유저 ID로 유저 조회 - 성공")
//    void getUserById_Success() {
//        // given
//        Long userId = 1L;
//        User expectedUser = User.builder()
//                .id(userId)
//                .name("Test User")
//                .build();
//        given(userRepository.findById(userId)).willReturn(Optional.of(expectedUser));
//
//        // when
//        User actualUser = userService.getUserById(userId);
//
//        // then
//        assertThat(actualUser).isEqualTo(expectedUser);
//        then(userRepository).should().findById(userId);
//    }
//
//    @Test
//    @DisplayName("유저 ID로 유저 조회 - 유저 없음")
//    void getUserById_NotFound() {
//        // given
//        Long userId = 1L;
//        given(userRepository.findById(userId)).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> userService.getUserById(userId))
//                .isInstanceOf(NoSuchElementException.class)
//                .hasMessage("유저를 찾을 수 없습니다: " + userId);
//    }
//
//    @Test
//    @DisplayName("유저 업데이트 - 성공")
//    void updateUser_Success() {
//        // given
//        Long userId = 1L;
//        UserPatchRequest request = new UserPatchRequest("Updated Name", "updated.jpg", "KB", "123-456", "서울", Grade.GOLD, 4L);
//        User existingUser = User.builder()
//                .id(userId)
//                .name("Old Name")
//                .build();
//        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
//        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        User updatedUser = userService.updateUser(request, userId);
//
//        // then
//        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
//        assertThat(updatedUser.getProfileImageUrl()).isEqualTo("updated.jpg");
//        assertThat(updatedUser.getAccountBank()).isEqualTo("KB");
//        assertThat(updatedUser.getAccountNumber()).isEqualTo("123-456");
//        assertThat(updatedUser.getAddress()).isEqualTo("서울");
//        assertThat(updatedUser.getGrade()).isEqualTo(Grade.GOLD);
//        assertThat(updatedUser.getMannerScore()).isEqualTo(4L);
//        then(userRepository).should().findById(userId);
//        then(userRepository).should().save(eq(updatedUser));
//    }
//
//    @Test
//    @DisplayName("유저 업데이트 - userId null")
//    void updateUser_UserIdNull() {
//        // given
//        UserPatchRequest request = new UserPatchRequest(null, null, null, null, null, null, null);
//
//        // when & then
//        assertThatThrownBy(() -> userService.updateUser(request, null))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("유저아이디가 필요합니다");
//    }
//
//    @Test
//    @DisplayName("유저 업데이트 - 유저 없음")
//    void updateUser_NotFound() {
//        // given
//        Long userId = 1L;
//        UserPatchRequest request = new UserPatchRequest("Updated Name", null, null, null, null, null, null);
//        given(userRepository.findById(userId)).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> userService.updateUser(request, userId))
//                .isInstanceOf(NoSuchElementException.class)
//                .hasMessage("유저를 찾을 수 없습니다:" + userId);
//    }
//
//    @Test
//    @DisplayName("유저 리스트 조회")
//    void getUserList() {
//        // given
//        List<User> expectedUsers = List.of(
//                User.builder().id(1L).name("User1").build(),
//                User.builder().id(2L).name("User2").build()
//        );
//        given(userRepository.findAll()).willReturn(expectedUsers);
//
//        // when
//        List<User> actualUsers = userService.getUserList();
//
//        // then
//        assertThat(actualUsers).isEqualTo(expectedUsers);
//        then(userRepository).should().findAll();
//    }
//
//    @Test
//    @DisplayName("createOrUpdateUser - 기존 유저 업데이트")
//    void createOrUpdateUser_Existing() {
//        // given
//        String socialId = "social123";
//        SocialProvider socialProvider = SocialProvider.GOOGLE;
//        String email = "new@example.com";
//        String name = "Updated Name";
//        UserLoginRequest request = new UserLoginRequest(socialId, socialProvider, email, name);
//        User existingUser = User.builder()
//                .id(1L)
//                .socialId(socialId)
//                .socialProvider(socialProvider)
//                .email("old@example.com")
//                .name("Old Name")
//                .build();
//        given(userRepository.findBySocialIdAndSocialProvider(socialId, socialProvider)).willReturn(Optional.of(existingUser));
//
//        // when
//        User result = userService.createOrUpdateUser(request);
//
//        // then
//        assertThat(result.getId()).isEqualTo(1L);
//        assertThat(result.getEmail()).isEqualTo(email);
//        assertThat(result.getName()).isEqualTo(name);
//        // Note: No save is called in the implementation, so repository.save() is not mocked/verified here
//        then(userRepository).should().findBySocialIdAndSocialProvider(socialId, socialProvider);
//    }
//
//    @Test
//    @DisplayName("createOrUpdateUser - 신규 유저 생성")
//    void createOrUpdateUser_New() {
//        // given
//        String socialId = "social123";
//        SocialProvider socialProvider = SocialProvider.GOOGLE;
//        String email = "new@example.com";
//        String name = "New Name";
//        UserLoginRequest request = new UserLoginRequest(socialId, socialProvider, email, name);
//        given(userRepository.findBySocialIdAndSocialProvider(socialId, socialProvider)).willReturn(Optional.empty());
//
//        // when
//        User result = userService.createOrUpdateUser(request);
//
//        // then
//        assertThat(result.getSocialId()).isEqualTo(socialId);
//        assertThat(result.getSocialProvider()).isEqualTo(socialProvider);
//        assertThat(result.getEmail()).isEqualTo(email);
//        assertThat(result.getName()).isEqualTo(name);
//        assertThat(result.getId()).isNull(); // ID is not set as it's not saved
//        // Note: No save is called in the implementation
//        then(userRepository).should().findBySocialIdAndSocialProvider(socialId, socialProvider);
//    }
//}