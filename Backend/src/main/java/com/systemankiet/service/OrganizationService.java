package com.systemankiet.service;

import com.systemankiet.dto.CreateOrganizationRequest;
import com.systemankiet.dto.OrganizationDto;
import com.systemankiet.entity.Organization;
import com.systemankiet.entity.User;
import com.systemankiet.repository.OrganizationRepository;
import com.systemankiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrganizationDto createOrganization(CreateOrganizationRequest request) {
        if (organizationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Organizacja o tej nazwie juz istnieje");
        }
        Organization org = new Organization();
        org.setName(request.getName().trim());
        return OrganizationDto.fromEntity(organizationRepository.save(org));
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(OrganizationDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Dolaczenie zalogowanego uzytkownika do wybranej organizacji
    @Transactional
    public void joinOrganization(Long organizationId, User user) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new NoSuchElementException("Organizacja nie znaleziona"));
        user.setOrganization(org);
        userRepository.save(user);
    }
}
