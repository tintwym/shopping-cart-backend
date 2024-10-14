package com.shopping.cart.mapper;

import com.shopping.cart.dto.request.UpdateProfileRequest;
import com.shopping.cart.entity.Profile;

public class ProfileMapper {
    public static Profile fromUpdateProfileRequest(Profile profile, UpdateProfileRequest updateProfileRequest) {
        profile.setAddress1(updateProfileRequest.getAddress1());
        profile.setAddress2(updateProfileRequest.getAddress2());
        profile.setUnit(updateProfileRequest.getUnit());
        profile.setFloor(updateProfileRequest.getFloor());
        profile.setCity(updateProfileRequest.getCity());
        profile.setState(updateProfileRequest.getState());
        profile.setCountry(updateProfileRequest.getCountry());
        profile.setZipCode(updateProfileRequest.getZipCode());
        return profile;
    }
}
