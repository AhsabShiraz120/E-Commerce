package com.bookworm.member;

import com.bookworm.api.MeApi;
import com.bookworm.api.model.Address;
import com.bookworm.api.model.AddressRequest;
import com.bookworm.api.model.UpdateProfileRequest;
import com.bookworm.api.model.User;
import com.bookworm.common.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController implements MeApi {

    private final MemberService memberService;

    @Override
    public ResponseEntity<User> getMe() {
        return ResponseEntity.ok(memberService.getMe(AuthenticatedUser.requireCurrentUserId()));
    }

    @Override
    public ResponseEntity<User> updateMe(UpdateProfileRequest updateProfileRequest) {
        return ResponseEntity.ok(memberService.updateMe(AuthenticatedUser.requireCurrentUserId(),
                updateProfileRequest));
    }

    @Override
    public ResponseEntity<List<Address>> listMyAddresses() {
        return ResponseEntity.ok(memberService.listAddresses(AuthenticatedUser.requireCurrentUserId()));
    }

    @Override
    public ResponseEntity<Address> addMyAddress(AddressRequest addressRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                memberService.addAddress(AuthenticatedUser.requireCurrentUserId(), addressRequest));
    }

    @Override
    public ResponseEntity<Address> updateMyAddress(Long id, AddressRequest addressRequest) {
        return ResponseEntity.ok(memberService.updateAddress(AuthenticatedUser.requireCurrentUserId(),
                id, addressRequest));
    }

    @Override
    public ResponseEntity<Void> deleteMyAddress(Long id) {
        memberService.deleteAddress(AuthenticatedUser.requireCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
