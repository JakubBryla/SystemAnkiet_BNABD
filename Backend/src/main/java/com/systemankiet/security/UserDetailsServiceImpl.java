package com.systemankiet.security;

import com.systemankiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementacja UserDetailsService wymagana przez Spring Security.
 * Ładuje użytkownika z bazy po emailu — używana przez DaoAuthenticationProvider przy logowaniu
 * oraz przez JwtAuthenticationFilter przy weryfikacji tokenu na każdym żądaniu.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Uzytkownik nie znaleziony: " + email));
    }
}
