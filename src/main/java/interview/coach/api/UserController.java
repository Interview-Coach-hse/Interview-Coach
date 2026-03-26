package interview.coach.api;

import interview.coach.generated.api.UserApi;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.UserResponse> userGet() {
        AppUserPrincipal principal = GeneratedApiSupport.currentPrincipal();
        return ResponseEntity.ok(GeneratedApiSupport.toGenerated(userService.getCurrentUserProfile(principal)));
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.UserResponse> userPatch(
            @Valid @RequestBody interview.coach.generated.model.UpdateUserRequest request
    ) {
        AppUserPrincipal principal = GeneratedApiSupport.currentPrincipal();
        return ResponseEntity.ok(GeneratedApiSupport.toGenerated(
                userService.updateCurrentUser(principal, GeneratedApiSupport.toInternal(request))
        ));
    }
}
