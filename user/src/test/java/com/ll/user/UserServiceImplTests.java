package com.ll.user;

import com.ll.user.exception.UserNotFoundException;
import com.ll.user.messaging.producer.UserEventProducer;
import com.ll.user.model.entity.User;
import com.ll.user.model.enums.SocialProvider;
import com.ll.user.model.vo.request.UserLoginRequest;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.user.model.vo.response.UserLoginResponse;
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.repository.UserRepository;
import com.ll.user.service.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer userEventProducer;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USER_CODE = "U12345";
    private static final String SOCIAL_ID = "social123";
    private static final SocialProvider SOCIAL_PROVIDER = SocialProvider.GOOGLE;

    // 리플렉션으로 BaseEntity 필드 주입 (id, code)
    private void setBaseEntityField(User user, String fieldName, Object value) {
        try {
            Field field = user.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(user, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set BaseEntity field: " + fieldName, e);
        }
    }

    private User createTestUser() {
        User user = User.builder()
                .socialId(SOCIAL_ID)
                .socialProvider(SOCIAL_PROVIDER)
                .email("test@example.com")
                .name("Test User")
                .build();

        setBaseEntityField(user, "id", TEST_USER_ID);
        setBaseEntityField(user, "code", TEST_USER_CODE);
        return user;
    }

    private User createAnotherUser() {
        User user = User.builder()
                .socialId("social456")
                .socialProvider(SocialProvider.NAVER)
                .email("user2@example.com")
                .name("User Two")
                .build();

        setBaseEntityField(user, "id", 2L);
        setBaseEntityField(user, "code", "U67890");
        return user;
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("성공: ID로 조회")
        void success() {
            User user = createTestUser();
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

            UserResponse response = userService.getUserById(TEST_USER_ID);

            assertThat(response.id()).isEqualTo(TEST_USER_ID);
            assertThat(response.code()).isEqualTo(TEST_USER_CODE);
            verify(userRepository).findById(TEST_USER_ID);
        }

        @Test
        @DisplayName("실패: 존재하지 않음")
        void notFound() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userService.getUserById(TEST_USER_ID))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getUserByUserCode")
    class GetUserByUserCode {

        @Test
        @DisplayName("성공: 코드로 조회")
        void success() {
            User user = createTestUser();
            when(userRepository.findByCode(TEST_USER_CODE)).thenReturn(Optional.of(user));

            UserResponse response = userService.getUserByUserCode(TEST_USER_CODE);

            assertThat(response.code()).isEqualTo(TEST_USER_CODE);
            verify(userRepository).findByCode(TEST_USER_CODE);
        }

        @Test
        @DisplayName("실패: 존재하지 않음")
        void notFound() {
            when(userRepository.findByCode(TEST_USER_CODE)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userService.getUserByUserCode(TEST_USER_CODE))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("성공: 부분 업데이트")
        void success() {
            User user = createTestUser();

            UserPatchRequest request = new UserPatchRequest(
                    "New Name", null, "new@example.com", null, null, null, null, null,null
            );

            when(userRepository.findByCode(TEST_USER_CODE)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            doAnswer(inv -> {
                User u = inv.getArgument(1);
                try {
                    Field name = User.class.getDeclaredField("name");  name.setAccessible(true);  name.set(u, "New Name");
                    Field email = User.class.getDeclaredField("email"); email.setAccessible(true); email.set(u, "new@example.com");
                } catch (Exception ignored) {}
                return null;
            }).when(modelMapper).map(request, user);

            UserResponse response = userService.updateUser(request, TEST_USER_CODE);

            verify(userRepository).save(user);
            assertThat(response.email()).isEqualTo("new@example.com");
            assertThat(response.name()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("실패: 존재하지 않음")
        void notFound() {
            UserPatchRequest request = new UserPatchRequest("New name",null,"new@example.com",null,null,null,null,null,null);
            when(userRepository.findByCode(TEST_USER_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(request, TEST_USER_CODE))
                    .isInstanceOf(UserNotFoundException.class);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getUserList")
    class GetUserList {

        @Test
        @DisplayName("성공: 전체 조회")
        void success() {
            User user1 = createTestUser();
            User user2 = createAnotherUser();

            when(userRepository.findAll()).thenReturn(List.of(user1, user2));

            List<UserResponse> responses = userService.getUserList();

            assertThat(responses).hasSize(2);
            assertThat(responses).extracting(UserResponse::code)
                    .containsExactlyInAnyOrder(TEST_USER_CODE, "U67890");
        }

        @Test
        @DisplayName("성공: 빈 리스트")
        void empty() {
            when(userRepository.findAll()).thenReturn(List.of());
            assertThat(userService.getUserList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("createOrUpdateUser")
    class CreateOrUpdateUser {

        @Test
        @DisplayName("성공: 신규 사용자 생성")
        void createNew() {
            UserLoginRequest request = new UserLoginRequest(
                    SOCIAL_ID, SOCIAL_PROVIDER, "new@example.com", "New User"
            );

            when(userRepository.findBySocialIdAndSocialProvider(SOCIAL_ID, SOCIAL_PROVIDER))
                    .thenReturn(Optional.empty());

            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                setBaseEntityField(saved, "id", 1L);
                setBaseEntityField(saved, "code", "U1");
                return saved;
            });

            UserLoginResponse response = userService.createOrUpdateUser(request);

            verify(userRepository).save(argThat(u ->
                    u.getSocialId().equals(SOCIAL_ID) &&
                            u.getEmail().equals("new@example.com") &&
                            u.getName().equals("New User")
            ));
            assertThat(response.code()).isEqualTo("U1");
        }

        @Test
        @DisplayName("성공: 기존 사용자 업데이트")
        void updateExisting() {
            User existing = spy(createTestUser());

            UserLoginRequest request = new UserLoginRequest(
                    SOCIAL_ID, SOCIAL_PROVIDER, "updated@example.com", "Updated Name"
            );

            when(userRepository.findBySocialIdAndSocialProvider(SOCIAL_ID, SOCIAL_PROVIDER))
                    .thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            UserLoginResponse response = userService.createOrUpdateUser(request);

            verify(existing).updateSocialInfo("updated@example.com", "Updated Name");
            verify(userRepository).save(existing);

            assertThat(response.email()).isEqualTo("updated@example.com");
            assertThat(response.name()).isEqualTo("Updated Name");
        }
    }
}