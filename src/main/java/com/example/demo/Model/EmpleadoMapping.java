package com.example.demo.Model;

import com.example.demo.entity.EmpleadoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmpleadoMapping {

    @Mapping(target = "rolId", source = "rol.id")
    @Mapping(target = "rolCodigo", source = "rol.codigo")
    EmpleadoModel convert(EmpleadoEntity entidad);

    List<EmpleadoModel> convert(List<EmpleadoEntity> lista);
}
