package gr.hua.it21774.service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gr.hua.it21774.dto.CommonUserDTO;
import gr.hua.it21774.enums.ERole;
import gr.hua.it21774.exceptions.GenericException;
import gr.hua.it21774.respository.RoleRepository;
import gr.hua.it21774.respository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Page<CommonUserDTO> getPagedUsers(Integer pageNumber, String pageSize, List<ERole> roles, Boolean enabled, String query) {
        Pageable pageable;

        if (pageSize.equals("ALL")) {
            pageable = Pageable.unpaged();
        } else {
            pageable = PageRequest.of(pageNumber, Integer.parseInt(pageSize));
        }

        return userRepository.customFindAll(pageable, roles, enabled, query);
    }

    public Page<CommonUserDTO> getPagedHuaUsers(Integer pageNumber, String pageSize, List<ERole> roles,
            Boolean enabled, String query) {
        Pageable pageable;

        if (pageSize.equals("ALL")) {
            pageable = Pageable.unpaged();
        } else {
            pageable = PageRequest.of(pageNumber, Integer.parseInt(pageSize));
        }

        return userRepository.customFindAllHua(pageable, roles, enabled, query);
    }

    @Transactional
    public void updateUser(Long id, Boolean isEnabled) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Claims accessTokenClaims = (Claims) authentication.getDetails();

        Instant lastModified = Instant.now();
        Long lastModifiedBy = Long.valueOf(accessTokenClaims.getSubject());

        int result = userRepository.updateUser(id, isEnabled, lastModified, lastModifiedBy);

        if (result == 0) {
            throw new GenericException(HttpStatus.BAD_REQUEST, "Something went wrong");
        }
    }

    public CommonUserDTO getUserProfile(String username) {
        CommonUserDTO profile = userRepository.getUserProfile(username)
                .orElse(new CommonUserDTO());

        return profile;
    }

    public List<CommonUserDTO> getProfessors(String query, List<Long> excludedIds) {

        return userRepository.assignProfessors(query, excludedIds);
    }

    public List<CommonUserDTO> searchStudents(String query) {

        Long studentRoleId = roleRepository.findIdByRole(ERole.STUDENT).get();

        return userRepository.searchStudents(query, studentRoleId);
    }
}
