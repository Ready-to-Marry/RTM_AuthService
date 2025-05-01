package ready_to_marry.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ready_to_marry.authservice.entity.user.Users;
import ready_to_marry.authservice.repository.TestRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final TestRepository testRepository;

    @GetMapping("/")
    public String index() {
        return "Health Check";
    }

    @PostMapping("/add")
    public ResponseEntity<String> add() {
        Users user = Users.builder()
                .name("dummy")
                .build();
        testRepository.save(user);
        return ResponseEntity.ok("dummy add ok");
    }
}
