package com.bookworm.member;

import com.bookworm.api.model.User;
import com.bookworm.api.model.UserRole;
import com.bookworm.member.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "giftPoints", target = "giftPointsBalance")
    User toDto(UserEntity entity);

    @ValueMapping(source = "GUEST",    target = "GUEST")
    @ValueMapping(source = "CUSTOMER", target = "CUSTOMER")
    @ValueMapping(source = "ADMIN",    target = "ADMIN")
    UserRole toDtoRole(com.bookworm.member.entity.UserRole role);
}
