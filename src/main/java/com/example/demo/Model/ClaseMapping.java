package com.example.demo.Model;

import com.example.demo.entity.AsistenciaClaseEntity;
import com.example.demo.entity.ClaseEntity;
import com.example.demo.entity.SocioClaseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClaseMapping {

    @Mapping(target = "inscritos", ignore = true)
    @Mapping(target = "instructorId", source = "instructor.id")
    @Mapping(target = "instructorNombre", expression = "java(entidad.getInstructor().getNombre() + \" \" + entidad.getInstructor().getApellido())")
    @Mapping(target = "sedeId", source = "sede.id")
    @Mapping(target = "sedeNombre", source = "sede.nombre")
    ClaseModel convert(ClaseEntity entidad);

    @Mapping(target = "socioId", source = "socio.id")
    @Mapping(target = "socioNombre", expression = "java(entidad.getSocio().getNombre() + \" \" + (entidad.getSocio().getApellido() == null ? \"\" : entidad.getSocio().getApellido()))")
    @Mapping(target = "claseId", source = "clase.id")
    @Mapping(target = "claseNombre", source = "clase.nombre")
    SocioClaseModel convert(SocioClaseEntity entidad);

    List<SocioClaseModel> convertInscripciones(List<SocioClaseEntity> lista);

    @Mapping(target = "socioClaseId", source = "socioClase.id")
    @Mapping(target = "socioId", source = "socioClase.socio.id")
    @Mapping(target = "socioNombre", expression = "java(entidad.getSocioClase().getSocio().getNombre() + \" \" + (entidad.getSocioClase().getSocio().getApellido() == null ? \"\" : entidad.getSocioClase().getSocio().getApellido()))")
    @Mapping(target = "claseId", source = "socioClase.clase.id")
    @Mapping(target = "claseNombre", source = "socioClase.clase.nombre")
    @Mapping(target = "registradoPorId", source = "registradoPor.id")
    AsistenciaClaseModel convert(AsistenciaClaseEntity entidad);

    List<AsistenciaClaseModel> convertAsistencias(List<AsistenciaClaseEntity> lista);
}
