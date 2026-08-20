package com.bookworm.member;

import com.bookworm.api.model.Address;
import com.bookworm.api.model.AddressRequest;
import com.bookworm.member.entity.AddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    Address toDto(AddressEntity entity);

    AddressEntity toEntity(AddressRequest req);

    void updateEntity(AddressRequest req, @MappingTarget AddressEntity entity);
}
