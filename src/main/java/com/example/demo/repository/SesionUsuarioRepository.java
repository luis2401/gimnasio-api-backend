package com.example.demo.repository;

import com.example.demo.entity.SesionUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SesionUsuarioRepository extends JpaRepository<SesionUsuarioEntity, Long> {

    Optional<SesionUsuarioEntity> findByTokenHash(String tokenHash);

    @Query("select s from SesionUsuarioEntity s where s.usuario.id = :usuarioId "
            + "and s.revocadaEn is null and s.expiraEn > :ahora order by s.creadaEn desc")
    List<SesionUsuarioEntity> findActivasByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("ahora") Instant ahora);

    @Modifying
    @Query("update SesionUsuarioEntity s set s.revocadaEn = :ahora "
            + "where s.usuario.id = :usuarioId and s.revocadaEn is null")
    int revocarTodasDeUsuario(@Param("usuarioId") Long usuarioId, @Param("ahora") Instant ahora);

    @Modifying
    @Query("delete from SesionUsuarioEntity s where s.expiraEn < :limite")
    int eliminarExpiradasAntesDe(@Param("limite") Instant limite);
}
