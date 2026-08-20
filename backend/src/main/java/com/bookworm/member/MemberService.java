package com.bookworm.member;

import com.bookworm.api.model.Address;
import com.bookworm.api.model.AddressRequest;
import com.bookworm.api.model.UpdateProfileRequest;
import com.bookworm.api.model.User;
import com.bookworm.common.ApiException;
import com.bookworm.member.entity.AddressEntity;
import com.bookworm.member.entity.UserEntity;
import com.bookworm.member.repo.AddressRepository;
import com.bookworm.member.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    @Transactional(readOnly = true)
    public User getMe(Long userId) {
        return userMapper.toDto(loadUser(userId));
    }

    @Transactional
    public User updateMe(Long userId, UpdateProfileRequest req) {
        UserEntity user = loadUser(userId);
        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName()  != null) user.setLastName(req.getLastName());
        if (req.getPhone()     != null) user.setPhone(req.getPhone());
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<Address> listAddresses(Long userId) {
        return addressRepository.findAllByUserIdOrderByIsDefaultDescIdAsc(userId).stream()
                .map(addressMapper::toDto)
                .toList();
    }

    @Transactional
    public Address addAddress(Long userId, AddressRequest req) {
        AddressEntity a = addressMapper.toEntity(req);
        a.setUserId(userId);
        if (Boolean.TRUE.equals(a.getIsDefault())) {
            clearOtherDefaults(userId);
        } else if (addressRepository.findAllByUserIdOrderByIsDefaultDescIdAsc(userId).isEmpty()) {
            a.setIsDefault(Boolean.TRUE);
        }
        return addressMapper.toDto(addressRepository.save(a));
    }

    @Transactional
    public Address updateAddress(Long userId, Long addressId, AddressRequest req) {
        AddressEntity a = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> ApiException.notFound("Address " + addressId + " not found"));
        addressMapper.updateEntity(req, a);
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            clearOtherDefaults(userId);
            a.setIsDefault(Boolean.TRUE);
        }
        return addressMapper.toDto(a);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        AddressEntity a = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> ApiException.notFound("Address " + addressId + " not found"));
        addressRepository.delete(a);
    }

    // ------------------------------------------------------------------------------

    private UserEntity loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User " + id + " not found"));
    }

    private void clearOtherDefaults(Long userId) {
        addressRepository.findAllByUserIdAndIsDefaultTrue(userId)
                .forEach(other -> other.setIsDefault(Boolean.FALSE));
    }
}
