package com.example.demo.Model;

import com.example.demo.dto.CreateSedeRequest;
import com.example.demo.dto.UpdateSedeRequest;
import com.example.demo.entity.SedeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SedeMapping {

    SedeModel convert(SedeEntity entidad);

    SedeEntity convert(SedeModel modelo);

    SedeEntity convert(CreateSedeRequest request);

    List<SedeModel> convert(List<SedeEntity> listaSedeEntidad);

    void copy(@MappingTarget SedeEntity entidad, UpdateSedeRequest request);
}
