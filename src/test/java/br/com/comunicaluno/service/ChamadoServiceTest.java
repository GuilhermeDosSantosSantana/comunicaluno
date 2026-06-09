package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.ChamadoDAO;
import br.com.comunicaluno.model.Chamado;
import br.com.comunicaluno.model.ChamadoMensagem;
import br.com.comunicaluno.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChamadoServiceTest {

    @Test
    void alunoAbreChamadoComStatusAbertoSemResponsavel() {
        FakeChamadoDAO dao = new FakeChamadoDAO();
        ChamadoService service = new ChamadoService(dao);
        Usuario aluno = usuario(1, "ALUNO", "ATIVO");

        service.abrirChamado(new Chamado("  Secretaria  ", "  Preciso de declaração  "), aluno);

        assertEquals(1, dao.salvo.getIdAluno());
        assertEquals("ABERTO", dao.salvo.getStatus());
        assertNull(dao.salvo.getIdResponsavel());
        assertEquals("Secretaria", dao.salvo.getAssunto());
        assertEquals("Preciso de declaração", dao.salvo.getDescricao());
    }

    @Test
    void somenteAlunoAtivoPodeAbrirChamado() {
        ChamadoService service = new ChamadoService(new FakeChamadoDAO());

        assertThrows(SecurityException.class,
                () -> service.abrirChamado(new Chamado("Assunto", "Descrição"), usuario(2, "PROF", "ATIVO")));
        assertThrows(SecurityException.class,
                () -> service.abrirChamado(new Chamado("Assunto", "Descrição"), usuario(1, "ALUNO", "PENDENTE")));
        assertThrows(IllegalArgumentException.class,
                () -> service.abrirChamado(new Chamado("", "Descrição"), usuario(1, "ALUNO", "ATIVO")));
        assertThrows(IllegalArgumentException.class,
                () -> service.abrirChamado(new Chamado("Assunto", ""), usuario(1, "ALUNO", "ATIVO")));
    }

    @Test
    void alunoListaApenasSeusChamadosEEquipeListaFilaGeral() {
        FakeChamadoDAO dao = new FakeChamadoDAO();
        ChamadoService service = new ChamadoService(dao);

        service.listarChamados(usuario(5, "ALUNO", "ATIVO"));
        assertEquals(5, dao.ultimoAlunoConsultado);
        assertEquals(0, dao.consultasFilaGeral);

        service.listarChamados(usuario(2, "COORDENADOR", "ATIVO"));
        assertEquals(1, dao.consultasFilaGeral);
    }

    @Test
    void equipeAssumeEAtualizaStatusDoChamado() {
        FakeChamadoDAO dao = new FakeChamadoDAO();
        ChamadoService service = new ChamadoService(dao);
        Usuario professor = usuario(8, "PROF", "ATIVO");

        service.assumirChamado(10, professor);
        assertEquals(10, dao.ultimoChamadoAssumido);
        assertEquals(8, dao.ultimoResponsavel);

        service.alterarStatus(10, "RESOLVIDO", professor);
        assertEquals(10, dao.ultimoChamadoAtualizado);
        assertEquals("RESOLVIDO", dao.ultimoStatus);

        service.alterarStatus(10, "EM_ANALISE", professor);
        assertEquals(10, dao.ultimoChamadoAssumido);
        assertEquals(8, dao.ultimoResponsavel);
    }

    @Test
    void alunoNaoAtendeChamadoEStatusPrecisaSerValido() {
        ChamadoService service = new ChamadoService(new FakeChamadoDAO());
        Usuario aluno = usuario(1, "ALUNO", "ATIVO");
        Usuario admin = usuario(2, "ADMIN", "ATIVO");

        assertThrows(SecurityException.class, () -> service.assumirChamado(10, aluno));
        assertThrows(SecurityException.class, () -> service.alterarStatus(10, "RESOLVIDO", aluno));
        assertThrows(IllegalArgumentException.class, () -> service.alterarStatus(10, "INVALIDO", admin));
    }

    @Test
    void chatPermiteAlunoDonoEEquipeEnviarMensagens() {
        FakeChamadoDAO dao = new FakeChamadoDAO();
        ChamadoService service = new ChamadoService(dao);

        service.enviarMensagem(10, "  Olá  ", null, usuario(1, "ALUNO", "ATIVO"));
        assertEquals(10, dao.ultimaMensagem.getIdChamado());
        assertEquals(1, dao.ultimaMensagem.getIdAutor());
        assertEquals("Olá", dao.ultimaMensagem.getMensagem());

        service.enviarMensagem(10, "", "C:\\temp\\arquivo.png", usuario(8, "PROF", "ATIVO"));
        assertEquals("Arquivo anexado.", dao.ultimaMensagem.getMensagem());
        assertEquals("C:\\temp\\arquivo.png", dao.ultimaMensagem.getAnexoPath());
    }

    @Test
    void alunoNaoAcessaChamadoDeOutroAluno() {
        ChamadoService service = new ChamadoService(new FakeChamadoDAO());

        assertThrows(SecurityException.class,
                () -> service.enviarMensagem(10, "Mensagem", null, usuario(99, "ALUNO", "ATIVO")));
        assertThrows(SecurityException.class,
                () -> service.listarMensagens(10, usuario(99, "ALUNO", "ATIVO")));
    }

    @Test
    void arquivarChamadoRespeitaPerfil() {
        FakeChamadoDAO dao = new FakeChamadoDAO();
        ChamadoService service = new ChamadoService(dao);
        Chamado chamado = new Chamado("Assunto", "Descrição");
        chamado.setIdChamado(10);
        chamado.setIdAluno(1);
        chamado.setIdResponsavel(8);

        assertTrue(service.podeArquivar(chamado, usuario(2, "ADMIN", "ATIVO")));
        assertTrue(service.podeArquivar(chamado, usuario(3, "COORDENADOR", "ATIVO")));
        assertTrue(service.podeArquivar(chamado, usuario(8, "PROF", "ATIVO")));
        assertTrue(service.podeArquivar(chamado, usuario(1, "ALUNO", "ATIVO")));
        assertFalse(service.podeArquivar(chamado, usuario(9, "PROF", "ATIVO")));
        assertFalse(service.podeArquivar(chamado, usuario(4, "ALUNO", "ATIVO")));

        dao.chamadoParaBusca = chamado;
        service.arquivarChamado(10, usuario(1, "ALUNO", "ATIVO"), "Resolvido");
        assertEquals(10, dao.ultimoArquivado);
        assertEquals(1, dao.arquivadoPor);

        assertThrows(SecurityException.class, () -> service.arquivarChamado(10, usuario(9, "PROF", "ATIVO"), null));
    }

    private static Usuario usuario(int id, String perfil, String status) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNome("Usuário " + id);
        usuario.setEmail("usuario" + id + "@escola.com");
        usuario.setTipoPerfil(perfil);
        usuario.setStatusConta(status);
        return usuario;
    }

    private static class FakeChamadoDAO extends ChamadoDAO {
        private Chamado salvo;
        private Integer ultimoAlunoConsultado;
        private int consultasFilaGeral;
        private Integer ultimoChamadoAssumido;
        private Integer ultimoResponsavel;
        private Integer ultimoChamadoAtualizado;
        private String ultimoStatus;
        private ChamadoMensagem ultimaMensagem;
        private Chamado chamadoParaBusca;
        private Integer ultimoArquivado;
        private Integer arquivadoPor;

        @Override
        public void salvar(Chamado chamado) {
            this.salvo = chamado;
            chamado.setIdChamado(10);
        }

        @Override
        public List<Chamado> listarPorAluno(int idAluno) {
            this.ultimoAlunoConsultado = idAluno;
            return List.of();
        }

        @Override
        public List<Chamado> listarFilaGeral() {
            this.consultasFilaGeral++;
            return List.of();
        }

        @Override
        public void assumirChamado(int idChamado, int idResponsavel) {
            this.ultimoChamadoAssumido = idChamado;
            this.ultimoResponsavel = idResponsavel;
        }

        @Override
        public void atualizarStatus(int idChamado, String status) {
            this.ultimoChamadoAtualizado = idChamado;
            this.ultimoStatus = status;
        }

        @Override
        public Chamado buscarPorId(int idChamado) {
            if (chamadoParaBusca != null) {
                return chamadoParaBusca;
            }
            Chamado chamado = new Chamado("Assunto", "Descrição");
            chamado.setIdChamado(idChamado);
            chamado.setIdAluno(1);
            chamado.setStatus("ABERTO");
            return chamado;
        }

        @Override
        public void adicionarMensagem(ChamadoMensagem mensagem) {
            this.ultimaMensagem = mensagem;
        }

        @Override
        public List<ChamadoMensagem> listarMensagens(int idChamado) {
            return List.of();
        }

        @Override
        public void registrarHistoricoStatus(int idChamado, Integer idOperador, String statusAnterior, String statusNovo, String observacao) {
        }

        @Override
        public int contarPorStatus(String status) {
            return 0;
        }

        @Override
        public void arquivarChamado(int idChamado, int idUsuario, String motivo) {
            this.ultimoArquivado = idChamado;
            this.arquivadoPor = idUsuario;
        }
    }
}
