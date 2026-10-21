package com.example.demo.Model;

import com.example.demo.entity.AsignacionSedeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AsignacionSedeMapping {

    @Mapping(target = "empleadoId", source = "empleado.id")
    @Mapping(target = "empleadoNombre", expression = "java(entidad.getEmpleado().getNombre() + \" \" + entidad.getEmpleado().getApellido())")
    @Mapping(target = "sedeId", source = "sede.id")
    @Mapping(target = "sedeNombre", source = "sede.nombre")
    AsignacionSedeModel convert(AsignacionSedeEntity entidad);

    List<AsignacionSedeModel> convert(List<AsignacionSedeEntity> lista);
}
