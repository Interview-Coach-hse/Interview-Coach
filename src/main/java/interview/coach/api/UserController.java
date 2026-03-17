package interview.coach.api;

import interview.coach.api.dto.UserDtos.UpdateUserRequest;
import interview.coach.api.dto.UserDtos.UserResponse;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> currentUser(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(principal));
    }

    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateCurrentUser(principal, request));
    }
}
