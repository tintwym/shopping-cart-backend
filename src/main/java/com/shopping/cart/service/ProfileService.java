package com.shopping.cart.service;

import com.shopping.cart.dto.request.UpdateProfileRequest;
import com.shopping.cart.entity.Profile;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.IProfileService;
import com.shopping.cart.mapper.ProfileMapper;
import com.shopping.cart.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService implements IProfileService {
    private final ProfileRepository profileRepository;
    private final UserService userService;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, UserService userService) {
        this.profileRepository = profileRepository;
        this.userService = userService;
    }

    @Override
    public void updateProfile(String token, UpdateProfileRequest updateProfileRequest) {
        // Get the user ID from the token
        User user = userService.getUserFromToken(token);

        // Get the user's profile from the database
        Profile profile = profileRepository.findByUserId(user.getId());

        // Update the profile
        Profile updatedProfile = ProfileMapper.fromUpdateProfileRequest(profile, updateProfileRequest);

        // Save the updated profile to the database
        profileRepository.save(updatedProfile);
    }
}
