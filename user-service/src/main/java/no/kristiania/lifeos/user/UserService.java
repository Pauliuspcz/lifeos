package no.kristiania.lifeos.user;
import no.kristiania.lifeos.user.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepo repo;

    public UserService(UserRepo repo) {
        this.repo = repo;
    }
    public User save(User user) {
        return repo.save(user);
    }
    public List<User> findAll() {
        return repo.findAll();
    }
    public User findById(Long id) {
        return repo.findById(id).orElseThrow(()-> new NotFoundException("User not found"));
    }
    public void deleteById(Long id) {
        User user = repo.findById(id).orElseThrow(()-> new NotFoundException("User not found"));
        repo.delete(user);
    }
}
