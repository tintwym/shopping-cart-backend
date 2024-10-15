package com.shopping.cart.controller.api;

import com.shopping.cart.dto.request.UpdateProfileRequest;
import com.shopping.cart.entity.Profile;
import com.shopping.cart.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/profiles")
public class ProfileApiController {
    private final ProfileService profileService;

    @Autowired
    public ProfileApiController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/show")
    public Profile getProfile(@RequestHeader("Authorization") String token) {
        return profileService.getProfile(token);
    }

    @PostMapping("/update")
    public void updateProfile(@RequestHeader("Authorization") String token, @RequestBody UpdateProfileRequest updateProfileRequest) {
        profileService.updateProfile(token, updateProfileRequest);
    }
}