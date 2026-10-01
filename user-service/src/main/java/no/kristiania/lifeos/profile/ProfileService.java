/*package no.kristiania.lifeos.profile;
import no.kristiania.lifeos.exceptions.NotFoundException;
import no.kristiania.lifeos.user.User;
import no.kristiania.lifeos.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {
    private final UserRepository repo;

    public ProfileService(UserRepository repo) {
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
}*/
