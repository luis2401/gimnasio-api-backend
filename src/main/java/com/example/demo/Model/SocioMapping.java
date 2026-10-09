package com.example.demo.Model;

import com.example.demo.dto.CreateSocioRequest;
import com.example.demo.dto.UpdateSocioRequest;
import com.example.demo.entity.SocioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SocioMapping {

    SocioModel convert(SocioEntity entidad);

    SocioEntity convert(SocioModel modelo);

    SocioEntity convert(CreateSocioRequest createSocioRequest);

    List<SocioModel> convert(List<SocioEntity> listaSocioEntidad);

    void copy(@MappingTarget SocioEntity oldSocio, UpdateSocioRequest request);

    SocioModel convert(UpdateSocioRequest updateSocioRequest);
}
