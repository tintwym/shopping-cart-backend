package com.shopping.cart.interfaces;

import com.shopping.cart.dto.request.UpdateProfileRequest;

public interface IProfileService {
    void updateProfile(String token, UpdateProfileRequest updateProfileRequest);
}
