package com.shopping.cart.controller.api;

import com.shopping.cart.dto.request.UpdateProfileRequest;
import com.shopping.cart.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/profiles")
public class ProfileApiController {
    private final ProfileService profileService;

    @Autowired
    public ProfileApiController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/update")
    public void updateProfile(@RequestHeader("Authorization") String token, UpdateProfileRequest updateProfileRequest) {
        profileService.updateProfile(token, updateProfileRequest);
    }
}
