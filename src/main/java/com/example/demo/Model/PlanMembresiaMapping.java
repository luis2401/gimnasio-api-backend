package com.example.demo.Model;

import com.example.demo.dto.CreatePlanMembresiaRequest;
import com.example.demo.dto.UpdatePlanMembresiaRequest;
import com.example.demo.entity.PlanMembresiaEntity;
import com.example.demo.entity.PlanSedeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PlanMembresiaMapping {

    PlanMembresiaModel convert(PlanMembresiaEntity entidad);

    @Mapping(target = "preciosSedes", ignore = true)
    PlanMembresiaEntity convert(CreatePlanMembresiaRequest request);

    List<PlanMembresiaModel> convert(List<PlanMembresiaEntity> listaEntidad);

    @Mapping(target = "planId", source = "plan.id")
    @Mapping(target = "planNombre", source = "plan.nombre")
    @Mapping(target = "sedeId", source = "sede.id")
    @Mapping(target = "sedeCodigo", source = "sede.codigo")
    @Mapping(target = "sedeNombre", source = "sede.nombre")
    PlanSedeModel convert(PlanSedeEntity entidad);

    List<PlanSedeModel> convertPrecios(List<PlanSedeEntity> listaPrecios);

    @Mapping(target = "preciosSedes", ignore = true)
    void copy(@MappingTarget PlanMembresiaEntity entidad, UpdatePlanMembresiaRequest request);
}
