package br.com.thalesleao.todolist.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import at.favre.lib.crypto.bcrypt.BCrypt;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<UserModel> create(@RequestBody UserModel user) {
        if (this.userRepository.findByUsername(user.getUsername()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário informado já existe.");
        }

        user.setPassword(BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray()));

        return ResponseEntity.status(HttpStatus.CREATED).body(this.userRepository.save(user));
    }
}