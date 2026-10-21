package com.example.demo.Model;

import com.example.demo.entity.PagoEntity;
import com.example.demo.entity.SuscripcionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VentaMapping {

    @Mapping(target = "socioId", source = "socio.id")
    @Mapping(target = "socioNombre", expression = "java(entidad.getSocio().getNombre() + \" \" + (entidad.getSocio().getApellido() == null ? \"\" : entidad.getSocio().getApellido()))")
    @Mapping(target = "planSedeId", source = "planSede.id")
    @Mapping(target = "sedeId", source = "planSede.sede.id")
    @Mapping(target = "sedeNombre", source = "planSede.sede.nombre")
    SuscripcionModel convert(SuscripcionEntity entidad);

    List<SuscripcionModel> convertSuscripciones(List<SuscripcionEntity> lista);

    @Mapping(target = "suscripcionId", source = "suscripcion.id")
    @Mapping(target = "sedeId", source = "sede.id")
    @Mapping(target = "sedeNombre", source = "sede.nombre")
    @Mapping(target = "vendedorId", source = "vendedor.id")
    @Mapping(target = "vendedorNombre", expression = "java(entidad.getVendedor().getNombre() + \" \" + entidad.getVendedor().getApellido())")
    @Mapping(target = "anuladoPorId", source = "anuladoPor.id")
    PagoModel convert(PagoEntity entidad);

    List<PagoModel> convertPagos(List<PagoEntity> lista);
}
