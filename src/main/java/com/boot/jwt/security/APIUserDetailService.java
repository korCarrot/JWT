package com.boot.jwt.security;

import com.boot.jwt.domain.APIUser;
import com.boot.jwt.dto.APIUserDTO;
import com.boot.jwt.repository.APIUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class APIUserDetailService implements UserDetailsService {

    private final APIUserRepository apiUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<APIUser> result = apiUserRepository.findById(username);

        APIUser apiUser = result.orElseThrow(() -> new UsernameNotFoundException("Cannot find mid"));

        log.info("APIUserDetailService apiUser---------------------------");

        APIUserDTO dto = new APIUserDTO(
          apiUser.getMid(),
          apiUser.getMpw(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));  //List.of로 생성된 리스트는 수정할 수 없습니다. 추가, 삭제, 수정 작업을 시도하면 UnsupportedOperationException이 발생합니다. null 값을 포함할 수 없습니다.

        log.info(dto);

        return dto;
    }
}
