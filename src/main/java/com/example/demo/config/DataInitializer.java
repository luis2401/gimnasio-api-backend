package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

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
    private AsignacionSedeRepository asignacionSedeRepository;

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ClaseRepository claseRepository;

    @Autowired
    private SocioClaseRepository socioClaseRepository;

    @Autowired
    private AsistenciaClaseRepository asistenciaClaseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. SEDES
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

        SedeEntity sedeNorte = sedeRepository.findByCodigo("SEDE-NORTE")
                .orElseGet(() -> sedeRepository.save(SedeEntity.builder()
                        .codigo("SEDE-NORTE")
                        .nombre("Sede Yanahuara")
                        .direccion("Av. Ejército 710, Yanahuara")
                        .activa(true)
                        .build()));

        // 2. PLANES DE MEMBRESIA Y TARIFAS POR SEDE
        if (planMembresiaRepository.count() == 0) {
            planMembresiaRepository.save(PlanMembresiaEntity.builder()
                    .nombre("Plan Mensual Classic")
                    .duracionDias(30)
                    .activo(true)
                    .condiciones("Acceso a máquinas de musculación y área cardiovascular.")
                    .build());

            planMembresiaRepository.save(PlanMembresiaEntity.builder()
                    .nombre("Plan Trimestral Pro")
                    .duracionDias(90)
                    .activo(true)
                    .condiciones("Acceso a todas las máquinas, cardio y 1 evaluación física mensual.")
                    .build());

            planMembresiaRepository.save(PlanMembresiaEntity.builder()
                    .nombre("Plan Anual Black VIP")
                    .duracionDias(365)
                    .activo(true)
                    .condiciones("Acceso total ilimitado, clases grupales e ingreso multisede.")
                    .build());
        }

        List<PlanMembresiaEntity> todosPlanes = planMembresiaRepository.findAll();
        List<SedeEntity> todasSedes = sedeRepository.findAll();
        for (PlanMembresiaEntity plan : todosPlanes) {
            for (SedeEntity sede : todasSedes) {
                if (planSedeRepository.findByPlanIdAndSedeId(plan.getId(), sede.getId()).isEmpty()) {
                    BigDecimal precioBase = plan.getDuracionDias() <= 35
                            ? new BigDecimal("120.00")
                            : plan.getDuracionDias() <= 100
                            ? new BigDecimal("320.00")
                            : new BigDecimal("1100.00");
                    BigDecimal recargo = plan.getDuracionDias() > 180
                            ? BigDecimal.ZERO
                            : new BigDecimal("25.00");
                    planSedeRepository.save(PlanSedeEntity.builder()
                            .plan(plan)
                            .sede(sede)
                            .precioActual(precioBase)
                            .recargoMultisede(recargo)
                            .activo(true)
                            .build());
                }
            }
        }

        // 3. ROLES
        RolEntity rolAdmin = rolRepository.findByCodigo("ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(RolEntity.builder().codigo("ADMINISTRADOR").nombre("Administrador").activo(true).build()));
        RolEntity rolSupervisor = rolRepository.findByCodigo("SUPERVISOR")
                .orElseGet(() -> rolRepository.save(RolEntity.builder().codigo("SUPERVISOR").nombre("Supervisor").activo(true).build()));
        RolEntity rolInstructor = rolRepository.findByCodigo("INSTRUCTOR")
                .orElseGet(() -> rolRepository.save(RolEntity.builder().codigo("INSTRUCTOR").nombre("Instructor").activo(true).build()));
        RolEntity rolRecepcionista = rolRepository.findByCodigo("RECEPCIONISTA")
                .orElseGet(() -> rolRepository.save(RolEntity.builder().codigo("RECEPCIONISTA").nombre("Recepcionista").activo(true).build()));

        // 4. EMPLEADOS (Todos los roles representados)
        EmpleadoEntity empAdmin = empleadoRepository.findByDni("00000001")
                .orElseGet(() -> empleadoRepository.save(EmpleadoEntity.builder()
                        .dni("00000001")
                        .nombre("Admin")
                        .apellido("Principal")
                        .rol(rolAdmin)
                        .telefono("999111222")
                        .correo("admin@yanfitness.com")
                        .especialidad("Dirección General")
                        .activo(true)
                        .build()));

        EmpleadoEntity empRecepcionista = empleadoRepository.findByDni("00000002")
                .orElseGet(() -> empleadoRepository.save(EmpleadoEntity.builder()
                        .dni("00000002")
                        .nombre("Empleado")
                        .apellido("Recepcion")
                        .rol(rolRecepcionista)
                        .telefono("999222333")
                        .correo("recepcion@yanfitness.com")
                        .especialidad("Atención y Caja")
                        .activo(true)
                        .build()));

        EmpleadoEntity empSupervisor = empleadoRepository.findByDni("71234501")
                .orElseGet(() -> empleadoRepository.save(EmpleadoEntity.builder()
                        .dni("71234501")
                        .nombre("Carlos")
                        .apellido("Gutiérrez")
                        .rol(rolSupervisor)
                        .telefono("987111222")
                        .correo("c.gutierrez@yanfitness.com")
                        .direccion("Av. Arequipa 1420")
                        .especialidad("Supervisión Operativa")
                        .activo(true)
                        .build()));

        EmpleadoEntity empInstructor1 = empleadoRepository.findByDni("71234502")
                .orElseGet(() -> empleadoRepository.save(EmpleadoEntity.builder()
                        .dni("71234502")
                        .nombre("Rodrigo")
                        .apellido("Navarro")
                        .rol(rolInstructor)
                        .telefono("987333444")
                        .correo("r.navarro@yanfitness.com")
                        .direccion("Calle Los Pinos 340")
                        .especialidad("Musculación y CrossFit")
                        .activo(true)
                        .build()));

        EmpleadoEntity empInstructor2 = empleadoRepository.findByDni("71234503")
                .orElseGet(() -> empleadoRepository.save(EmpleadoEntity.builder()
                        .dni("71234503")
                        .nombre("Camila")
                        .apellido("Vargas")
                        .rol(rolInstructor)
                        .telefono("987555666")
                        .correo("c.vargas@yanfitness.com")
                        .direccion("Av. Primavera 890")
                        .especialidad("Spinning y Funcional")
                        .activo(true)
                        .build()));

        EmpleadoEntity empRecep2 = empleadoRepository.findByDni("71234504")
                .orElseGet(() -> empleadoRepository.save(EmpleadoEntity.builder()
                        .dni("71234504")
                        .nombre("Lucía")
                        .apellido("Paredes")
                        .rol(rolRecepcionista)
                        .telefono("987777888")
                        .correo("l.paredes@yanfitness.com")
                        .direccion("Jr. Unión 215")
                        .especialidad("Atención al Socio y Ventas")
                        .activo(true)
                        .build()));

        // 5. USUARIOS DEL SISTEMA
        UsuarioEntity adminUser = usuarioRepository.findByUsername("admin")
                .orElseGet(() -> usuarioRepository.save(UsuarioEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("123456"))
                        .empleado(empAdmin)
                        .activo(true)
                        .fechaCreacion(new Date())
                        .build()));

        UsuarioEntity recepcionUser = usuarioRepository.findByUsername("empleado")
                .orElseGet(() -> usuarioRepository.save(UsuarioEntity.builder()
                        .username("empleado")
                        .password(passwordEncoder.encode("123456"))
                        .empleado(empRecepcionista)
                        .activo(true)
                        .fechaCreacion(new Date())
                        .build()));

        if (!usuarioRepository.existsByUsername("supervisor") && usuarioRepository.findByEmpleadoId(empSupervisor.getId()).isEmpty()) {
            usuarioRepository.save(UsuarioEntity.builder()
                    .username("supervisor")
                    .password(passwordEncoder.encode("123456"))
                    .empleado(empSupervisor)
                    .activo(true)
                    .fechaCreacion(new Date())
                    .build());
        }

        if (!usuarioRepository.existsByUsername("instructor") && usuarioRepository.findByEmpleadoId(empInstructor1.getId()).isEmpty()) {
            usuarioRepository.save(UsuarioEntity.builder()
                    .username("instructor")
                    .password(passwordEncoder.encode("123456"))
                    .empleado(empInstructor1)
                    .activo(true)
                    .fechaCreacion(new Date())
                    .build());
        }

        // 6. ASIGNACIONES DE SEDE (Personal operativo: Instructores y Recepcionistas)
        LocalDate hoy = LocalDate.now();
        if (asignacionSedeRepository.findByEmpleadoIdOrderByFechaDesdeDesc(empInstructor1.getId()).isEmpty()) {
            asignacionSedeRepository.save(AsignacionSedeEntity.builder()
                    .empleado(empInstructor1)
                    .sede(sedeCentral)
                    .fechaDesde(hoy.minusDays(30))
                    .fechaHasta(null)
                    .anulada(false)
                    .build());
        }
        if (asignacionSedeRepository.findByEmpleadoIdOrderByFechaDesdeDesc(empInstructor2.getId()).isEmpty()) {
            asignacionSedeRepository.save(AsignacionSedeEntity.builder()
                    .empleado(empInstructor2)
                    .sede(sedeSur)
                    .fechaDesde(hoy.minusDays(20))
                    .fechaHasta(null)
                    .anulada(false)
                    .build());
        }
        if (asignacionSedeRepository.findByEmpleadoIdOrderByFechaDesdeDesc(empRecep2.getId()).isEmpty()) {
            asignacionSedeRepository.save(AsignacionSedeEntity.builder()
                    .empleado(empRecep2)
                    .sede(sedeNorte)
                    .fechaDesde(hoy.minusDays(15))
                    .fechaHasta(hoy.plusDays(45))
                    .anulada(false)
                    .build());
        }

        // 7. SOCIOS DE PRUEBA
        SocioEntity socio1 = socioRepository.findByDni("45891230")
                .orElseGet(() -> socioRepository.save(SocioEntity.builder()
                        .dni("45891230")
                        .nombre("Alejandro")
                        .apellido("Ríos")
                        .activo(true)
                        .telefono("991234567")
                        .correo("alejandro.rios@gmail.com")
                        .fechaCreacion(new Date())
                        .usuario(adminUser)
                        .build()));

        SocioEntity socio2 = socioRepository.findByDni("72849102")
                .orElseGet(() -> socioRepository.save(SocioEntity.builder()
                        .dni("72849102")
                        .nombre("Valeria")
                        .apellido("Cárdenas")
                        .activo(true)
                        .telefono("992345678")
                        .correo("valeria.cardenas@gmail.com")
                        .fechaCreacion(new Date())
                        .usuario(adminUser)
                        .build()));

        SocioEntity socio3 = socioRepository.findByDni("73918273")
                .orElseGet(() -> socioRepository.save(SocioEntity.builder()
                        .dni("73918273")
                        .nombre("Sebastián")
                        .apellido("Torres")
                        .activo(false) // Membresía vencida / para probar Pase Diario o renovación
                        .telefono("993456789")
                        .correo("sebastian.torres@gmail.com")
                        .fechaCreacion(new Date())
                        .usuario(recepcionUser)
                        .build()));

        SocioEntity socio4 = socioRepository.findByDni("74829164")
                .orElseGet(() -> socioRepository.save(SocioEntity.builder()
                        .dni("74829164")
                        .nombre("Fernanda")
                        .apellido("Mendoza")
                        .activo(false) // Recién registrada sin membresía aún
                        .telefono("994567890")
                        .correo("fernanda.mendoza@gmail.com")
                        .fechaCreacion(new Date())
                        .usuario(recepcionUser)
                        .build()));

        // 8. SUSCRIPCIONES Y PAGOS
        PlanSedeEntity planSedeCentral = planSedeRepository.findBySedeIdAndActivo(sedeCentral.getId(), true)
                .stream().findFirst().orElse(null);
        PlanSedeEntity planSedeSur = planSedeRepository.findBySedeIdAndActivo(sedeSur.getId(), true)
                .stream().findFirst().orElse(null);

        if (planSedeCentral != null && suscripcionRepository.findBySocioIdOrderByFechaInicioDesc(socio1.getId()).isEmpty()) {
            SuscripcionEntity sub1 = suscripcionRepository.save(SuscripcionEntity.builder()
                    .socio(socio1)
                    .planSede(planSedeCentral)
                    .nombrePlanVendido(planSedeCentral.getPlan().getNombre())
                    .duracionDiasVendida(planSedeCentral.getPlan().getDuracionDias())
                    .precioLista(planSedeCentral.getPrecioActual().add(planSedeCentral.getRecargoMultisede()))
                    .precioVendido(planSedeCentral.getPrecioActual().add(planSedeCentral.getRecargoMultisede()))
                    .condicionesVendidas(planSedeCentral.getPlan().getCondiciones())
                    .fechaInicio(hoy.minusDays(5))
                    .fechaFin(hoy.plusDays(25))
                    .estado(EstadoSuscripcion.ACTIVA)
                    .accesoMultisede(true)
                    .recargoMultisede(planSedeCentral.getRecargoMultisede())
                    .build());

            pagoRepository.save(PagoEntity.builder()
                    .monto(sub1.getPrecioVendido())
                    .metodo(MetodoPago.YAPE)
                    .suscripcion(sub1)
                    .sede(sedeCentral)
                    .vendedor(empAdmin)
                    .numeroOperacion("OP-YAPE-900101")
                    .estado(EstadoPago.PAGADO)
                    .tipoVenta(TipoVenta.MEMBRESIA)
                    .build());
        }

        if (planSedeSur != null && suscripcionRepository.findBySocioIdOrderByFechaInicioDesc(socio2.getId()).isEmpty()) {
            SuscripcionEntity sub2 = suscripcionRepository.save(SuscripcionEntity.builder()
                    .socio(socio2)
                    .planSede(planSedeSur)
                    .nombrePlanVendido(planSedeSur.getPlan().getNombre())
                    .duracionDiasVendida(planSedeSur.getPlan().getDuracionDias())
                    .precioLista(planSedeSur.getPrecioActual())
                    .precioVendido(planSedeSur.getPrecioActual())
                    .condicionesVendidas(planSedeSur.getPlan().getCondiciones())
                    .fechaInicio(hoy.minusDays(25))
                    .fechaFin(hoy.plusDays(5)) // Por vencer en 5 días
                    .estado(EstadoSuscripcion.ACTIVA)
                    .accesoMultisede(false)
                    .recargoMultisede(BigDecimal.ZERO)
                    .build());

            pagoRepository.save(PagoEntity.builder()
                    .monto(sub2.getPrecioVendido())
                    .metodo(MetodoPago.TARJETA)
                    .suscripcion(sub2)
                    .sede(sedeSur)
                    .vendedor(empRecepcionista)
                    .numeroOperacion("OP-POS-900102")
                    .estado(EstadoPago.PAGADO)
                    .tipoVenta(TipoVenta.MEMBRESIA)
                    .build());
        }

        if (planSedeCentral != null && suscripcionRepository.findBySocioIdOrderByFechaInicioDesc(socio3.getId()).isEmpty()) {
            SuscripcionEntity subVencida = suscripcionRepository.save(SuscripcionEntity.builder()
                    .socio(socio3)
                    .planSede(planSedeCentral)
                    .nombrePlanVendido(planSedeCentral.getPlan().getNombre())
                    .duracionDiasVendida(30)
                    .precioLista(planSedeCentral.getPrecioActual())
                    .precioVendido(planSedeCentral.getPrecioActual())
                    .condicionesVendidas(planSedeCentral.getPlan().getCondiciones())
                    .fechaInicio(hoy.minusDays(40))
                    .fechaFin(hoy.minusDays(10))
                    .estado(EstadoSuscripcion.VENCIDA)
                    .accesoMultisede(false)
                    .recargoMultisede(BigDecimal.ZERO)
                    .build());

            pagoRepository.save(PagoEntity.builder()
                    .monto(subVencida.getPrecioVendido())
                    .metodo(MetodoPago.EFECTIVO)
                    .suscripcion(subVencida)
                    .sede(sedeCentral)
                    .vendedor(empRecepcionista)
                    .estado(EstadoPago.PAGADO)
                    .tipoVenta(TipoVenta.MEMBRESIA)
                    .build());

            // Pase diario de prueba
            pagoRepository.save(PagoEntity.builder()
                    .monto(new BigDecimal("15.00"))
                    .metodo(MetodoPago.PLIN)
                    .sede(sedeCentral)
                    .vendedor(empRecepcionista)
                    .numeroOperacion("OP-PLIN-900103")
                    .estado(EstadoPago.PAGADO)
                    .tipoVenta(TipoVenta.PASE_DIARIO)
                    .visitanteNombre("Sebastián Torres")
                    .visitanteDocumento("73918273")
                    .fechaAcceso(hoy)
                    .build());
        }

        // 9. CLASES GRUPALES, INSCRIPCIONES Y ASISTENCIAS
        if (claseRepository.count() == 0) {
            ClaseEntity clase1 = claseRepository.save(ClaseEntity.builder()
                    .nombre("CrossFit & Fuerza Funcional")
                    .diaSemana((short) 1) // Lunes
                    .horaInicio(LocalTime.of(7, 0))
                    .horaFin(LocalTime.of(8, 0))
                    .cupoMax(20)
                    .instructor(empInstructor1)
                    .sede(sedeCentral)
                    .activo(true)
                    .build());

            ClaseEntity clase2 = claseRepository.save(ClaseEntity.builder()
                    .nombre("Spinning Endurance")
                    .diaSemana((short) 2) // Martes
                    .horaInicio(LocalTime.of(19, 0))
                    .horaFin(LocalTime.of(20, 0))
                    .cupoMax(18)
                    .instructor(empInstructor2)
                    .sede(sedeSur)
                    .activo(true)
                    .build());

            ClaseEntity clase3 = claseRepository.save(ClaseEntity.builder()
                    .nombre("Full Body & Core HIIT")
                    .diaSemana((short) hoy.getDayOfWeek().getValue()) // Día de hoy
                    .horaInicio(LocalTime.of(18, 0))
                    .horaFin(LocalTime.of(19, 0))
                    .cupoMax(25)
                    .instructor(empInstructor1)
                    .sede(sedeCentral)
                    .activo(true)
                    .build());

            ClaseEntity clase4 = claseRepository.save(ClaseEntity.builder()
                    .nombre("Movilidad y Entrenamiento Metabólico")
                    .diaSemana((short) 6) // Sábado
                    .horaInicio(LocalTime.of(9, 0))
                    .horaFin(LocalTime.of(10, 30))
                    .cupoMax(15)
                    .instructor(empInstructor2)
                    .sede(sedeNorte)
                    .activo(true)
                    .build());

            SocioClaseEntity insc1 = socioClaseRepository.save(SocioClaseEntity.builder()
                    .socio(socio1)
                    .clase(clase1)
                    .fechaInscripcion(hoy.minusDays(5))
                    .activo(true)
                    .build());

            SocioClaseEntity insc2 = socioClaseRepository.save(SocioClaseEntity.builder()
                    .socio(socio1)
                    .clase(clase3)
                    .fechaInscripcion(hoy.minusDays(5))
                    .activo(true)
                    .build());

            SocioClaseEntity insc3 = socioClaseRepository.save(SocioClaseEntity.builder()
                    .socio(socio2)
                    .clase(clase2)
                    .fechaInscripcion(hoy.minusDays(10))
                    .activo(true)
                    .build());

            if (asistenciaClaseRepository.count() == 0) {
                asistenciaClaseRepository.save(AsistenciaClaseEntity.builder()
                        .socioClase(insc2)
                        .fecha(hoy)
                        .estado(EstadoAsistencia.ASISTIO)
                        .registradoPor(empInstructor1)
                        .observacion("Asistencia puntual")
                        .build());

                asistenciaClaseRepository.save(AsistenciaClaseEntity.builder()
                        .socioClase(insc1)
                        .fecha(hoy.minusDays(1))
                        .estado(EstadoAsistencia.ASISTIO)
                        .registradoPor(empInstructor1)
                        .observacion("Sesión completada")
                        .build());

                asistenciaClaseRepository.save(AsistenciaClaseEntity.builder()
                        .socioClase(insc3)
                        .fecha(hoy.minusDays(2))
                        .estado(EstadoAsistencia.JUSTIFICADO)
                        .registradoPor(empInstructor2)
                        .observacion("Aviso previo en recepción")
                        .build());
            }
        }
    }
}