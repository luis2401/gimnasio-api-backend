package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private PlanMembresiaRepository planMembresiaRepository;

    @Autowired
    private PlanSedeRepository planSedeRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        SedeEntity sedeCentral = sedeRepository.findByCodigo("SEDE-CENTRAL")
                .orElseGet(() -> sedeRepository.save(SedeEntity.builder()
                        .codigo("SEDE-CENTRAL")
                        .nombre("Sede Central")
                        .direccion("Av. Universitaria 1801, Lima")
                        .activa(true)
                        .build()));

        SedeEntity sedeSur = sedeRepository.findByCodigo("SEDE-SUR")
                .orElseGet(() -> sedeRepository.save(SedeEntity.builder()
                        .codigo("SEDE-SUR")
                        .nombre("Sede Sur")
                        .direccion("Av. Primavera 450, Santiago de Surco")
                        .activa(true)
                        .build()));

        if (planMembresiaRepository.count() == 0) {
            PlanMembresiaEntity planMensual = planMembresiaRepository.save(PlanMembresiaEntity.builder()
                    .nombre("Plan Mensual Classic")
                    .duracionDias(30)
                    .activo(true)
                    .condiciones("Acceso a máquinas y área cardiovascular.")
                    .build());

            PlanMembresiaEntity planTrimestral = planMembresiaRepository.save(PlanMembresiaEntity.builder()
                    .nombre("Plan Trimestral Pro")
                    .duracionDias(90)
                    .activo(true)
                    .condiciones("Acceso a todas las máquinas, cardio y 1 asesoría mensual.")
                    .build());

            PlanMembresiaEntity planAnual = planMembresiaRepository.save(PlanMembresiaEntity.builder()
                    .nombre("Plan Anual Black VIP")
                    .duracionDias(365)
                    .activo(true)
                    .condiciones("Acceso total ilimitado, clases grupales y acceso multisede.")
                    .build());

            planSedeRepository.save(PlanSedeEntity.builder()
                    .plan(planMensual)
                    .sede(sedeCentral)
                    .precioActual(new BigDecimal("120.00"))
                    .recargoMultisede(new BigDecimal("20.00"))
                    .activo(true)
                    .build());

            planSedeRepository.save(PlanSedeEntity.builder()
                    .plan(planMensual)
                    .sede(sedeSur)
                    .precioActual(new BigDecimal("130.00"))
                    .recargoMultisede(new BigDecimal("20.00"))
                    .activo(true)
                    .build());

            planSedeRepository.save(PlanSedeEntity.builder()
                    .plan(planTrimestral)
                    .sede(sedeCentral)
                    .precioActual(new BigDecimal("320.00"))
                    .recargoMultisede(new BigDecimal("30.00"))
                    .activo(true)
                    .build());

            planSedeRepository.save(PlanSedeEntity.builder()
                    .plan(planTrimestral)
                    .sede(sedeSur)
                    .precioActual(new BigDecimal("350.00"))
                    .recargoMultisede(new BigDecimal("30.00"))
                    .activo(true)
                    .build());

            planSedeRepository.save(PlanSedeEntity.builder()
                    .plan(planAnual)
                    .sede(sedeCentral)
                    .precioActual(new BigDecimal("1100.00"))
                    .recargoMultisede(BigDecimal.ZERO)
                    .activo(true)
                    .build());

            planSedeRepository.save(PlanSedeEntity.builder()
                    .plan(planAnual)
                    .sede(sedeSur)
                    .precioActual(new BigDecimal("1200.00"))
                    .recargoMultisede(BigDecimal.ZERO)
                    .activo(true)
                    .build());
        }

        if (usuarioRepository.count() > 0) {
            return;
        }

        RolEntity rolAdmin = rolRepository.findByCodigo("ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(RolEntity.builder().codigo("ADMINISTRADOR").nombre("Administrador").activo(true).build()));

        RolEntity rolRecepcionista = rolRepository.findByCodigo("RECEPCIONISTA")
                .orElseGet(() -> rolRepository.save(RolEntity.builder().codigo("RECEPCIONISTA").nombre("Recepcionista").activo(true).build()));

        EmpleadoEntity empAdmin = empleadoRepository.findByDni("00000001")
                .orElseGet(() -> empleadoRepository.save(EmpleadoEntity.builder()
                        .dni("00000001")
                        .nombre("Admin")
                        .apellido("Principal")
                        .rol(rolAdmin)
                        .telefono("999111222")
                        .correo("admin@gym.com")
                        .activo(true)
                        .build()));

        EmpleadoEntity empRecepcionista = empleadoRepository.findByDni("00000002")
                .orElseGet(() -> empleadoRepository.save(EmpleadoEntity.builder()
                        .dni("00000002")
                        .nombre("Empleado")
                        .apellido("Recepcion")
                        .rol(rolRecepcionista)
                        .telefono("999222333")
                        .correo("recepcion@gym.com")
                        .activo(true)
                        .build()));

        UsuarioEntity admin = UsuarioEntity.builder()
                .username("admin")
                .password(passwordEncoder.encode("123456"))
                .empleado(empAdmin)
                .activo(true)
                .fechaCreacion(new Date())
                .build();

        UsuarioEntity empleado = UsuarioEntity.builder()
                .username("empleado")
                .password(passwordEncoder.encode("123456"))
                .empleado(empRecepcionista)
                .activo(true)
                .fechaCreacion(new Date())
                .build();

        admin = usuarioRepository.save(admin);
        empleado = usuarioRepository.save(empleado);

        if (socioRepository.count() == 0) {
            socioRepository.save(SocioEntity.builder()
                    .dni("12345678")
                    .nombre("Juan")
                    .apellido("Perez")
                    .activo(true)
                    .telefono("999111222")
                    .correo("juan.perez@mail.com")
                    .fechaCreacion(new Date())
                    .usuario(admin)
                    .build());

            socioRepository.save(SocioEntity.builder()
                    .dni("87654321")
                    .nombre("Maria")
                    .apellido("Lopez")
                    .activo(false)
                    .telefono("977888999")
                    .correo("maria.lopez@mail.com")
                    .fechaCreacion(new Date())
                    .usuario(empleado)
                    .build());
        }
    }
}