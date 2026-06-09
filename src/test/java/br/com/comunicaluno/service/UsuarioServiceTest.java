package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.UsuarioDAO;
import br.com.comunicaluno.model.Usuario;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioServiceTest {

    @Test
    void cadastroValidoGeraHashPendenteEEmailNormalizado() {
        FakeUsuarioDAO dao = new FakeUsuarioDAO();
        UsuarioService service = new UsuarioService(dao);

        service.registarNovoUsuario(new Usuario(" Ana Silva ", "ANA@ESCOLA.COM", "senha123", "ALUNO"));

        Usuario salvo = dao.buscarPorEmail("ana@escola.com");
        assertEquals("Ana Silva", salvo.getNome());
        assertEquals("ana@escola.com", salvo.getEmail());
        assertEquals("PENDENTE", salvo.getStatusConta());
        assertNotEquals("senha123", salvo.getSenhaHash());
        assertTrue(BCrypt.checkpw("senha123", salvo.getSenhaHash()));
    }

    @Test
    void cadastroRejeitaDadosInvalidosEDuplicidade() {
        FakeUsuarioDAO dao = new FakeUsuarioDAO();
        dao.salvar(usuario(1, "Aluno", "aluno@escola.com", "ALUNO", "PENDENTE", "senha123"));
        UsuarioService service = new UsuarioService(dao);

        assertThrows(IllegalArgumentException.class,
                () -> service.registarNovoUsuario(new Usuario("", "novo@escola.com", "senha123", "ALUNO")));
        assertThrows(IllegalArgumentException.class,
                () -> service.registarNovoUsuario(new Usuario("Novo", "email-invalido", "senha123", "ALUNO")));
        assertThrows(IllegalArgumentException.class,
                () -> service.registarNovoUsuario(new Usuario("Novo", "novo@escola.com", "123", "ALUNO")));
        assertThrows(IllegalArgumentException.class,
                () -> service.registarNovoUsuario(new Usuario("Novo", "novo@escola.com", "senha123", "ADMIN")));
        assertThrows(IllegalArgumentException.class,
                () -> service.registarNovoUsuario(new Usuario("Novo", "aluno@escola.com", "senha123", "ALUNO")));
    }

    @Test
    void loginRetornaUsuarioAtivoEBloqueiaContaInativaOuPendente() {
        FakeUsuarioDAO dao = new FakeUsuarioDAO();
        dao.salvar(usuario(1, "Ativo", "ativo@escola.com", "ALUNO", "ATIVO", "senha123"));
        dao.salvar(usuario(2, "Pendente", "pendente@escola.com", "ALUNO", "PENDENTE", "senha123"));
        UsuarioService service = new UsuarioService(dao);

        assertEquals("Ativo", service.autenticar("ATIVO@ESCOLA.COM", "senha123").getNome());

        SecurityException bloqueio = assertThrows(SecurityException.class,
                () -> service.autenticar("pendente@escola.com", "senha123"));
        assertEquals("Conta pendente de aprovação ou inativa.", bloqueio.getMessage());

        SecurityException credenciais = assertThrows(SecurityException.class,
                () -> service.autenticar("ativo@escola.com", "senhaErrada"));
        assertEquals("E-mail ou palavra-passe incorretos.", credenciais.getMessage());
    }

    @Test
    void adminOuCoordenadorAprovaEInativaContasPendentes() {
        FakeUsuarioDAO dao = new FakeUsuarioDAO();
        dao.salvar(usuario(10, "Aluno", "aluno@escola.com", "ALUNO", "PENDENTE", "senha123"));
        UsuarioService service = new UsuarioService(dao);
        Usuario coordenador = usuario(2, "Coord", "coord@escola.com", "COORDENADOR", "ATIVO", "senha123");

        assertEquals(1, service.listarContasPendentes(coordenador).size());

        service.aprovarUsuario(10, coordenador);
        assertEquals("ATIVO", dao.buscarPorEmail("aluno@escola.com").getStatusConta());
        assertEquals(2, dao.buscarPorEmail("aluno@escola.com").getAprovadoPor());

        service.inativarUsuario(10, coordenador);
        assertEquals("INATIVO", dao.buscarPorEmail("aluno@escola.com").getStatusConta());

        Usuario aluno = usuario(3, "Aluno Dois", "aluno2@escola.com", "ALUNO", "ATIVO", "senha123");
        assertThrows(SecurityException.class, () -> service.listarContasPendentes(aluno));
    }

    private static Usuario usuario(int id, String nome, String email, String perfil, String status, String senhaLimpa) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTipoPerfil(perfil);
        usuario.setStatusConta(status);
        usuario.setSenhaHash(BCrypt.hashpw(senhaLimpa, BCrypt.gensalt(12)));
        return usuario;
    }

    private static class FakeUsuarioDAO extends UsuarioDAO {
        private final Map<String, Usuario> usuariosPorEmail = new LinkedHashMap<>();

        @Override
        public void salvar(Usuario usuario) {
            if (usuario.getIdUsuario() == null) {
                usuario.setIdUsuario(usuariosPorEmail.size() + 1);
            }
            usuariosPorEmail.put(usuario.getEmail(), usuario);
        }

        @Override
        public Usuario buscarPorEmail(String email) {
            return usuariosPorEmail.get(email);
        }

        @Override
        public boolean emailExiste(String email) {
            return usuariosPorEmail.containsKey(email);
        }

        @Override
        public List<Usuario> listarPorStatus(String statusConta) {
            List<Usuario> resultado = new ArrayList<>();
            for (Usuario usuario : usuariosPorEmail.values()) {
                if (statusConta.equals(usuario.getStatusConta())) {
                    resultado.add(usuario);
                }
            }
            return resultado;
        }

        @Override
        public void atualizarStatusConta(int idUsuario, String novoStatus, Integer aprovadoPor) {
            for (Usuario usuario : usuariosPorEmail.values()) {
                if (usuario.getIdUsuario() == idUsuario) {
                    usuario.setStatusConta(novoStatus);
                    usuario.setAprovadoPor(aprovadoPor);
                }
            }
        }
    }
}
