package com.shopping.cart.interfaces;

import com.shopping.cart.dto.request.UpdateProfileRequest;
import com.shopping.cart.entity.Profile;

import java.util.UUID;

public interface IProfileService {
    Profile getProfile(String token);
    void updateProfile(String token, UpdateProfileRequest updateProfileRequest);
}
